package kava.opcode.op;
public interface OpVisitor{
        public void visitNOOP();
        public void visitADD(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitSUB(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitMUL(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitDIV(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitMOD(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitI2F(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitI2L(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitI2D(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitL2I(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitL2F(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitL2D(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitF2D(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitF2I(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitF2L(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitD2I(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitD2L(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitD2F(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitCMP(kava.opcode.VarObject result,kava.opcode.VarObject v1,kava.opcode.VarObject v2);
        public void visitASSIGN(kava.opcode.VarObject result,kava.opcode.VarObject v1);
        public void visitPARAM(kava.opcode.VarObject result);
        public void visitLCONST(kava.opcode.VarObject result,kava.opcode.Constant v1);
        public void visitIFFALSE(kava.opcode.VarObject v1,Integer v2);
        public void visitIFTRUE(kava.opcode.VarObject v1,Integer v2);
        public void visitIFEQ(kava.opcode.VarObject v1,Integer v2);
        public void visitIFNE(kava.opcode.VarObject v1,Integer v2);
        public void visitIFLT(kava.opcode.VarObject v1,Integer v2);
        public void visitIFGT(kava.opcode.VarObject v1,Integer v2);
        public void visitIFLE(kava.opcode.VarObject v1,Integer v2);
        public void visitIFGE(kava.opcode.VarObject v1,Integer v2);
        public void visitGOTO(Integer v1);
        public void visitIINC(kava.opcode.VarObject v1,Integer v2);
        public void visitICONST(kava.opcode.VarObject result,Integer v1);
        public void visitLDC(kava.opcode.VarObject result,kava.opcode.Constant v1);
}