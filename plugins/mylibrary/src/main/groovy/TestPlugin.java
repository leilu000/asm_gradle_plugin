import com.leilu.base.BasePlugin;


public class TestPlugin extends BasePlugin<TestExtension> {

    @Override
    protected byte[] modifyClass(byte[] classData, com.android.build.api.transform.Status status) {
        return classData;
    }

    @Override
    protected TestExtension initSelfDefineExtension() {
        return new TestExtension();
    }
}
