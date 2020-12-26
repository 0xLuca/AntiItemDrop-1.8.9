package at.luca.antiitemdrop.transformer;

import net.labymod.core.asm.LabyModCoreMod;
import net.labymod.core.asm.global.ClassEditor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class NetHandlerPlayClientTransformer implements IClassTransformer {
    private final String netHandlerPlayClientName = "net.minecraft.client.network.NetHandlerPlayClient";
    private final String netHandlerPlayClientNameObfuscated = "bcy";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!name.equals(LabyModCoreMod.isObfuscated() ? netHandlerPlayClientNameObfuscated : netHandlerPlayClientName)) {
            return basicClass;
        }
        ClassEditor editor = new NetHandlerPlayClientEditor(LabyModCoreMod.isObfuscated());
        ClassNode node = new ClassNode();
        ClassReader reader = new ClassReader(basicClass);
        reader.accept(node, 0);
        editor.accept(name, node);
        ClassWriter writer = new ClassWriter(3);
        node.accept(writer);
        return writer.toByteArray();
    }
}
