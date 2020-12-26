package at.luca.antiitemdrop.transformer;

import at.luca.antiitemdrop.PacketHandler;
import net.labymod.core.asm.global.ClassEditor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.Optional;

public class NetHandlerPlayClientEditor extends ClassEditor {
    private final String addToSendQueueNameUnObf = "addToSendQueue";
    private final String addToSendQueueNameObf = "a";
    private final String addToSendQueueDescriptorUnObf = "(Lnet/minecraft/network/Packet;)V";
    private final String addToSendQueueDescriptorObf = "(Lff;)V";
    private final String shouldSendPacketDescriptorUnObf = "(Lnet/minecraft/network/Packet;)Z";
    private final String shouldSendPacketDescriptorObf = "(Lff;)Z";

    private final String addToSendQueueName;
    private final String addToSendQueueDescriptor;
    private final String shouldSendPacketDescriptor;

    public NetHandlerPlayClientEditor(boolean obfuscated) {
        super(ClassEditorType.CLASS_NODE);
        this.addToSendQueueName = obfuscated ? addToSendQueueNameObf : addToSendQueueNameUnObf;
        this.addToSendQueueDescriptor = obfuscated ? addToSendQueueDescriptorObf : addToSendQueueDescriptorUnObf;
        this.shouldSendPacketDescriptor = obfuscated ? shouldSendPacketDescriptorObf : shouldSendPacketDescriptorUnObf;
    }

    @Override
    public void accept(String name, ClassNode node) {
        getMethod(node, addToSendQueueName, addToSendQueueDescriptor).ifPresent(method -> {
            getVarInstruction(method, Opcodes.ALOAD, 0, Opcodes.GETFIELD).ifPresent(firstInstruction -> {
                getInstruction(method, Opcodes.INVOKEVIRTUAL, Opcodes.ALOAD).ifPresent(lastInstruction -> {
                    LabelNode skipLabel = new LabelNode();
                    InsnList beforeInstructions = new InsnList();
                    beforeInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    beforeInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(PacketHandler.class), "shouldSendPacket", shouldSendPacketDescriptor, false));
                    beforeInstructions.add(new JumpInsnNode(Opcodes.IFEQ, skipLabel));

                    method.instructions.insertBefore(firstInstruction, beforeInstructions);
                    method.instructions.insert(lastInstruction, skipLabel);
                });
            });
        });
    }

    private Optional<MethodNode> getMethod(ClassNode node, String name, String descriptor) {
        return node.methods.stream().filter(methodNode -> methodNode.name.equals(name) && methodNode.desc.equals(descriptor)).findFirst();
    }

    private Optional<AbstractInsnNode> getVarInstruction(MethodNode method, int opcode, int value, int nextOpcode) {
        return Arrays.stream(method.instructions.toArray()).filter(abstractInsnNode -> isRightVarInstruction(abstractInsnNode, opcode, value, nextOpcode)).findFirst();
    }

    private boolean isRightVarInstruction(AbstractInsnNode instruction, int opcode, int value, int nextOpcode) {
        return instruction.getOpcode() == opcode && ((VarInsnNode) instruction).var == value && instruction.getNext().getOpcode() == nextOpcode;
    }

    private Optional<AbstractInsnNode> getInstruction(MethodNode method, int opcode, int beforeOpcode) {
        return Arrays.stream(method.instructions.toArray()).filter(abstractInsnNode -> isRightInstruction(abstractInsnNode, opcode, beforeOpcode)).findFirst();
    }

    private boolean isRightInstruction(AbstractInsnNode instruction, int opcode, int beforeOpcode) {
        return instruction.getOpcode() == opcode && instruction.getPrevious().getOpcode() == beforeOpcode;
    }
}

