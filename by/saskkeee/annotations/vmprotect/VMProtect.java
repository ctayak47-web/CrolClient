
package by.saskkeee.annotations.vmprotect;

import by.saskkeee.annotations.vmprotect.CompileType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.METHOD})
public @interface VMProtect {
    public CompileType type();
}

