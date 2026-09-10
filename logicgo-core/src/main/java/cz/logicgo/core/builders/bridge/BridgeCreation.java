package cz.logicgo.core.builders.bridge;

public final class BridgeCreation extends BridgeBuilderBase<BridgeCreation> {

    public BridgeCreation() {
        super();
    }

    @Override
    protected BridgeCreation self() {
        return this;
    }
}
