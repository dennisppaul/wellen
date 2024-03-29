/*
 * Wellen
 *
 * This file is part of the *wellen* library (https://github.com/dennisppaul/wellen).
 * Copyright (c) 2022 Dennis P Paul.
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

public class AndroidProbe {
    public static boolean isAndroid() {
        try {
            try {
                Class.forName(android.os.Build.class.getName());
                return true;
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            }
        } catch (NoClassDefFoundError ignored) {
        }

//        try {
//            Class.forName(android.os.Build.class.getName());
//            return true;
//        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
//        }

        return false;
    }
}