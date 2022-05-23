package xasm.base;

import org.objectweb.asm.Opcodes;

public interface Const {
    // 构造方法名
    String CONSTRUCTOR_NAME = "<init>";
    // 静态代码块名
    String STATIC_BLOCK_NAME = "<clinit>";

    int ASM_VERSION = Opcodes.ASM7;
}
