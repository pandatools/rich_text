package run.halo.links.finders.impl;

import org.springframework.http.HttpStatus;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.exception.ExtensionException;

public class MyExtensionNotFoundException extends ExtensionException {

    public MyExtensionNotFoundException(GroupVersionKind gvk, String name) {
        super(HttpStatus.NOT_FOUND, "Extension " + gvk + "/" + name + " was not found.",
            null, null, new Object[] {gvk, name});
    }

}
