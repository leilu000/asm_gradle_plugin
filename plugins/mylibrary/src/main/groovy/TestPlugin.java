import com.leilu.base.BasePlugin;

import org.objectweb.asm.MethodVisitor;


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
