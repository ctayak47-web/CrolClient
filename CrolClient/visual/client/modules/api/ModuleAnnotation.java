
package crol.client.modules.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import crol.client.modules.api.Category;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface ModuleAnnotation {
    public String name();

    public Category category();

    public String description();
}

