/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2024 Dennis P Paul.
 *
 * This library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package wellen;

import processing.core.PApplet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * internal helper for MIDI classes that call methods on a sketch ( e.g <code>midi_note_on(int, int, int)</code> ).
 * <p>
 * callbacks are either invoked immediately on the MIDI thread ( default ) or queued and invoked on the processing
 * animation thread right before <code>draw()</code> ( see {@link #set_dispatch_on_draw_thread(boolean)} ).
 */
final class MidiCallbacks {

    private final Object fParent;
    private final String fOwnerName;
    private final ConcurrentLinkedQueue<Runnable> fQueue = new ConcurrentLinkedQueue<>();
    private final Set<String> fReportedErrors = new HashSet<>();
    private volatile boolean fDispatchOnDrawThread = false;
    private boolean fRegisteredWithPApplet = false;

    MidiCallbacks(Object parent, String owner_name) {
        fParent = parent;
        fOwnerName = owner_name;
    }

    /**
     * finds a method in the class hierarchy of <code>target</code>. public methods ( also inherited ones ) are
     * preferred. non-public methods are made accessible if possible.
     *
     * @return method or <code>null</code> if no such method exists
     */
    static Method find_method(Object target, String name, Class<?>... parameter_types) {
        if (target == null) {
            return null;
        }
        try {
            final Method mMethod = target.getClass().getMethod(name, parameter_types);
            if (!java.lang.reflect.Modifier.isPublic(mMethod.getDeclaringClass().getModifiers())) {
                /* public method of a non-public class ( e.g a private inner class ) */
                try {
                    mMethod.setAccessible(true);
                } catch (RuntimeException ignored) {
                }
            }
            return mMethod;
        } catch (NoSuchMethodException | SecurityException ignored) {
        }
        Class<?> mClass = target.getClass();
        while (mClass != null && mClass != Object.class) {
            try {
                final Method mMethod = mClass.getDeclaredMethod(name, parameter_types);
                try {
                    mMethod.setAccessible(true);
                } catch (RuntimeException ignored) {
                    /* e.g InaccessibleObjectException, invoke will report problem */
                }
                return mMethod;
            } catch (NoSuchMethodException | SecurityException ignored) {
            }
            mClass = mClass.getSuperclass();
        }
        return null;
    }

    Method find(String name, Class<?>... parameter_types) {
        return find_method(fParent, name, parameter_types);
    }

    Object parent() {
        return fParent;
    }

    /**
     * @param dispatch_on_draw_thread if <code>true</code> callbacks are queued and invoked on the processing
     *                                animation thread before <code>draw()</code>. this is only possible if the parent
     *                                is a {@link PApplet}.
     * @return <code>true</code> if the requested mode is active
     */
    synchronized boolean set_dispatch_on_draw_thread(boolean dispatch_on_draw_thread, Object pre_target) {
        if (dispatch_on_draw_thread) {
            if (!(fParent instanceof PApplet)) {
                System.err.println("+++ @" + fOwnerName + " / dispatching on draw thread requires the parent to be " +
                                           "a `PApplet`. callbacks are invoked immediately on the MIDI thread.");
                fDispatchOnDrawThread = false;
                return false;
            }
            if (!fRegisteredWithPApplet) {
                ((PApplet) fParent).registerMethod("pre", pre_target);
                fRegisteredWithPApplet = true;
            }
        }
        fDispatchOnDrawThread = dispatch_on_draw_thread;
        return true;
    }

    boolean is_dispatching_on_draw_thread() {
        return fDispatchOnDrawThread;
    }

    void invoke(Method method, Object... args) {
        if (method == null) {
            return;
        }
        if (fDispatchOnDrawThread) {
            fQueue.add(() -> invokeNow(method, args));
        } else {
            invokeNow(method, args);
        }
    }

    /**
     * invokes all queued callbacks. called from <code>pre()</code> on the animation thread.
     */
    void drain() {
        Runnable mRunnable;
        while ((mRunnable = fQueue.poll()) != null) {
            mRunnable.run();
        }
    }

    void dispose(Object pre_target) {
        synchronized (this) {
            if (fRegisteredWithPApplet && fParent instanceof PApplet) {
                try {
                    ((PApplet) fParent).unregisterMethod("pre", pre_target);
                } catch (RuntimeException ignored) {
                }
                fRegisteredWithPApplet = false;
            }
            fDispatchOnDrawThread = false;
        }
        fQueue.clear();
    }

    private void invokeNow(Method method, Object[] args) {
        try {
            method.invoke(fParent, args);
        } catch (InvocationTargetException ex) {
            reportOnce(method, ex.getCause() != null ? ex.getCause() : ex);
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            reportOnce(method, ex);
        }
    }

    private void reportOnce(Method method, Throwable t) {
        final String mKey = method.getName() + ":" + t.getClass().getName();
        final boolean mFirst;
        synchronized (fReportedErrors) {
            mFirst = fReportedErrors.add(mKey);
        }
        if (mFirst) {
            System.err.println("+++ @" + fOwnerName + " / problem calling `" + method.getName() + "` ( further " +
                                       "errors of this kind are not reported ):");
            t.printStackTrace();
        }
    }
}
