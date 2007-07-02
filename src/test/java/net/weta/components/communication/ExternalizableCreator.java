package net.weta.components.communication;

import java.io.Externalizable;

import net.weta.components.test.DummyExternalizable;

public class ExternalizableCreator {
    public DummyExternalizable create() {
        return new DummyExternalizable();
    }
}
