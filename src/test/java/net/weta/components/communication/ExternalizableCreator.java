package net.weta.components.communication;

import net.weta.components.test.DummyExternalizable;

public class ExternalizableCreator {
    public DummyExternalizable create() {
        return new DummyExternalizable();
    }
}
