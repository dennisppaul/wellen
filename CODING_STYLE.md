# Wellen / Coding Style

- *class fields* are prefixed with `f` :: `float fThreshold = 0.2f`
- *local variables* are prefixed with `m` :: `float mValue = 1.23f`
- *method parameters* ( or arguments ) of *public* methods are *lower case* and use `_` to separate words :: `float threshold_scale`
- *method parameters* ( or arguments ) of *private* methods are prefixed with `p` :: `float pIncrease`
- fields, variables and optionally parameters are declared `final` where possible
- names of *public* methods are *lower case* and use `_` to separate words :: `public void scale_threshold(float threshold_scale)`
- names of *private* methods use *camel case* :: `private void increaseThreshold(float pIncrease)`
- additionally *getters* and *setters* are prefixed with `get_` or `set_` :: `set_threshold(float threshold)` + `get_threshold()`
- constants are *upper case* and use `_` to separate words :: `static final int THRESHOLD_MAX = 1.0f`

```
public class MyClass {
    public static final int THRESHOLD_MAX = 1.0f;
    private float fThreshold = 0.2f;
    
    public void set_threshold(float threshold) {
        fThreshold = threshold;
    }

    public float get_threshold() {
        return fThreshold;
    }
    
    public void scale_threshold(float threshold_scale) {
        final float mValue = pScale * fThreshold;
        fThreshold = mValue;
    }
    
    private void increaseThreshold(float pIncrease) {
        fThreshold += pIncrease;        
    }
}
```