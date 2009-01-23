//
// Copyright 2007-2009 Qianyan Cai
// Under the terms of the GNU Lesser General Public License version 2.1
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Bytes;


public final class Opcode
{
	public static final byte AALOAD = 50;
	public static final byte AASTORE = 83;
	public static final byte ACONSTNULL = 1;
	public static final byte ALOAD = 25;
	public static final byte ALOAD0 = 42;
	public static final byte ALOAD1 = 43;
	public static final byte ALOAD2 = 44;
	public static final byte ALOAD3 = 45;
	public static final byte ANEWARRAY = (byte)189;
	public static final byte ARETURN = (byte)176;
	public static final byte ARRAYLENGTH = (byte)190;
	public static final byte ASTORE = 58;
	public static final byte ASTORE0 = 75;
	public static final byte ASTORE1 = 76;
	public static final byte ASTORE2 = 77;
	public static final byte ASTORE3 = 78;
	public static final byte ATHROW = (byte)191;
	public static final byte BALOAD = 51;
	public static final byte BASTORE = 84;
	public static final byte BIPUSH = 16;
	public static final byte CALOAD = 52;
	public static final byte CASTORE = 85;
	public static final byte CHECKCAST = (byte)192;
	public static final byte D2F = (byte)144;
	public static final byte D2I = (byte)142;
	public static final byte D2L = (byte)143;
	public static final byte DADD = 99;
	public static final byte DALOAD = 49;
	public static final byte DASTORE = 82;
	public static final byte DCMPG = (byte)152;
	public static final byte DCMPL = (byte)151;
	public static final byte DCONST0 = 14;
	public static final byte DCONST1 = 15;
	public static final byte DDIV = 111;
	public static final byte DLOAD = 24;
	public static final byte DLOAD0 = 38;
	public static final byte DLOAD1 = 39;
	public static final byte DLOAD2 = 40;
	public static final byte DLOAD3 = 41;
	public static final byte DMUL = 107;
	public static final byte DNEG = 119;
	public static final byte DREM = 115;
	public static final byte DRETURN = (byte)175;
	public static final byte DSTORE = 57;
	public static final byte DSTORE0 = 71;
	public static final byte DSTORE1 = 72;
	public static final byte DSTORE2 = 73;
	public static final byte DSTORE3 = 74;
	public static final byte DSUB = 103;
	/** Stack: x -> x x */
	public static final byte DUP = 89;
	/** Stack: y x -> x y x, stupid */
	public static final byte DUPI = 90;
	/** Stack: z y x -> x z y x, stupid */
	public static final byte DUPII = 91;
	/** Stack: xx -> xx xx */
	public static final byte DUP8 = 92;
	/** Stack: y xx -> xx y xx, stupid */
	public static final byte DUP8I = 93;
	/** Stack: z y xx -> xx z y xx, stupid */
	public static final byte DUP8II = 94;
	public static final byte F2D = (byte)141;
	public static final byte F2I = (byte)139;
	public static final byte F2L = (byte)140;
	public static final byte FADD = 98;
	public static final byte FALOAD = 48;
	public static final byte FASTORE = 81;
	public static final byte FCMPG = (byte)150;
	public static final byte FCMPL = (byte)149;
	public static final byte FCONST0 = 11;
	public static final byte FCONST1 = 12;
	public static final byte FCONST2 = 13;
	public static final byte FDIV = 110;
	public static final byte FLOAD = 23;
	public static final byte FLOAD0 = 34;
	public static final byte FLOAD1 = 35;
	public static final byte FLOAD2 = 36;
	public static final byte FLOAD3 = 37;
	public static final byte FMUL = 106;
	public static final byte FNEG = 118;
	public static final byte FREM = 114;
	public static final byte FRETURN = (byte)174;
	public static final byte FSTORE = 56;
	public static final byte FSTORE0 = 67;
	public static final byte FSTORE1 = 68;
	public static final byte FSTORE2 = 69;
	public static final byte FSTORE3 = 70;
	public static final byte FSUB = 102;
	public static final byte GETFIELD = (byte)180;
	public static final byte GETSTATIC = (byte)178;
	public static final byte GOTO = (byte)167;
	public static final byte GOTO4 = (byte)200;
	public static final byte I2B = (byte)145;
	public static final byte I2C = (byte)146;
	public static final byte I2D = (byte)135;
	public static final byte I2F = (byte)134;
	public static final byte I2L = (byte)133;
	public static final byte I2S = (byte)147;
	public static final byte IADD = (byte)96;
	public static final byte IALOAD = (byte)46;
	public static final byte IAND = 126;
	public static final byte IASTORE = 79;
	public static final byte ICONSTm1 = 2;
	public static final byte ICONST0 = 3;
	public static final byte ICONST1 = 4;
	public static final byte ICONST2 = 5;
	public static final byte ICONST3 = 6;
	public static final byte ICONST4 = 7;
	public static final byte ICONST5 = 8;
	public static final byte IDIV = 108;
	public static final byte IFAE = (byte)165;
	public static final byte IFAN = (byte)166;
	public static final byte IFIE = (byte)159;
	public static final byte IFIE0 = (byte)153;
	public static final byte IFIG = (byte)163;
	public static final byte IFIG0 = (byte)157;
	public static final byte IFIGE = (byte)162;
	public static final byte IFIGE0 = (byte)156;
	public static final byte IFIL = (byte)161;
	public static final byte IFIL0 = (byte)155;
	public static final byte IFILE = (byte)164;
	public static final byte IFILE0 = (byte)158;
	public static final byte IFIN = (byte)160;
	public static final byte IFIN0 = (byte)154;
	public static final byte IFNOTNULL = (byte)199;
	public static final byte IFNULL = (byte)198;
	public static final byte IINC = (byte)132;
	public static final byte ILOAD = 21;
	public static final byte ILOAD0 = 26;
	public static final byte ILOAD1 = 27;
	public static final byte ILOAD2 = 28;
	public static final byte ILOAD3 = 29;
	public static final byte IMUL = 104;
	public static final byte INEG = 116;
	public static final byte INSTANCEOF = (byte)193;
	public static final byte INVOKEINTERFACE = (byte)185;
	public static final byte INVOKESPECIAL = (byte)183;
	public static final byte INVOKESTATIC = (byte)184;
	public static final byte INVOKEVIRTUAL = (byte)182;
	public static final byte IOR = (byte)128;
	public static final byte IREM = 112;
	public static final byte IRETURN = (byte)172;
	public static final byte ISHL = 120;
	public static final byte ISHR = 122;
	public static final byte ISTORE = 54;
	public static final byte ISTORE0 = 59;
	public static final byte ISTORE1 = 60;
	public static final byte ISTORE2 = 61;
	public static final byte ISTORE3 = 62;
	public static final byte ISUB = 100;
	public static final byte IUSHR = 124;
	public static final byte IXOR = (byte)130;
	public static final byte JSR = (byte)168;
	public static final byte JSR4 = (byte)201;
	public static final byte L2D = (byte)138;
	public static final byte L2F = (byte)137;
	public static final byte L2I = (byte)136;
	public static final byte LADD = 97;
	public static final byte LALOAD = 47;
	public static final byte LAND = 127;
	public static final byte LASTORE = 80;
	public static final byte LCMP = (byte)148;
	public static final byte LCONST0 = 9;
	public static final byte LCONST1 = 10;
	public static final byte LDC = 18;
	public static final byte LDCW = 19;
	public static final byte LDC8W = 20;
	public static final byte LDIV = 109;
	public static final byte LLOAD = 22;
	public static final byte LLOAD0 = 30;
	public static final byte LLOAD1 = 31;
	public static final byte LLOAD2 = 32;
	public static final byte LLOAD3 = 33;
	public static final byte LMUL = 105;
	public static final byte LNEG = 117;
	public static final byte LOOKUPSWITCH = (byte)171;
	public static final byte LOR = (byte)129;
	public static final byte LREM = 113;
	public static final byte LRETURN = (byte)173;
	public static final byte LSHL = 121;
	public static final byte LSHR = 123;
	public static final byte LSTORE = 55;
	public static final byte LSTORE0 = 63;
	public static final byte LSTORE1 = 64;
	public static final byte LSTORE2 = 65;
	public static final byte LSTORE3 = 66;
	public static final byte LSUB = 101;
	public static final byte LUSHR = 125;
	public static final byte LXOR = (byte)131;
	public static final byte MONITORENTER = (byte)194;
	public static final byte MONITOREXIT = (byte)195;
	public static final byte MULTIANEWARRAY = (byte)197;
	public static final byte NEW = (byte)187;
	public static final byte NEWARRAY = (byte)188;
	public static final byte NOP = 0;
	public static final byte POP = 87;
	public static final byte POP8 = 88;
	public static final byte PUTFIELD = (byte)181;
	public static final byte PUTSTATIC = (byte)179;
	public static final byte RET = (byte)169;
	public static final byte RETURN = (byte)177;
	public static final byte SALOAD = 53;
	public static final byte SASTORE = 86;
	public static final byte SIPUSH = 17;
	public static final byte SWAP = 95;
	public static final byte TABLESWITCH = (byte)170;
	public static final byte WIDE = (byte)196;

	public static final byte NEWARRAY_BOOL = 4;
	public static final byte NEWARRAY_BYTE = 8;
	public static final byte NEWARRAY_SHORT = 9;
	public static final byte NEWARRAY_CHAR = 5;
	public static final byte NEWARRAY_INT = 10;
	public static final byte NEWARRAY_LONG = 11;
	public static final byte NEWARRAY_FLOAT = 6;
	public static final byte NEWARRAY_DOUBLE = 7;

	private Opcode()
	{
	}

	public static String toString(byte op)
	{
		switch (op)
		{
		case AALOAD:
			return "AALOAD";
		case AASTORE:
			return "AASTORE";
		case ACONSTNULL:
			return "ACONSTNULL";
		case ALOAD:
			return "ALOAD";
		case ALOAD0:
			return "ALOAD0";
		case ALOAD1:
			return "ALOAD1";
		case ALOAD2:
			return "ALOAD2";
		case ALOAD3:
			return "ALOAD3";
		case ANEWARRAY:
			return "ANEWARRAY";
		case ARETURN:
			return "ARETURN";
		case ARRAYLENGTH:
			return "ARRAYLENGTH";
		case ASTORE:
			return "ASTORE";
		case ASTORE0:
			return "ASTORE0";
		case ASTORE1:
			return "ASTORE1";
		case ASTORE2:
			return "ASTORE2";
		case ASTORE3:
			return "ASTORE3";
		case ATHROW:
			return "ATHROW";
		case BALOAD:
			return "BALOAD";
		case BASTORE:
			return "BASTORE";
		case BIPUSH:
			return "BIPUSH";
		case CALOAD:
			return "CALOAD";
		case CASTORE:
			return "CASTORE";
		case CHECKCAST:
			return "CHECKCAST";
		case D2F:
			return "D2F";
		case D2I:
			return "D2I";
		case D2L:
			return "D2L";
		case DADD:
			return "DADD";
		case DALOAD:
			return "DALOAD";
		case DASTORE:
			return "DASTORE";
		case DCMPG:
			return "DCMPG";
		case DCMPL:
			return "DCMPL";
		case DCONST0:
			return "DCONST0";
		case DCONST1:
			return "DCONST1";
		case DDIV:
			return "DDIV";
		case DLOAD:
			return "DLOAD";
		case DLOAD0:
			return "DLOAD0";
		case DLOAD1:
			return "DLOAD1";
		case DLOAD2:
			return "DLOAD2";
		case DLOAD3:
			return "DLOAD3";
		case DMUL:
			return "DMUL";
		case DNEG:
			return "DNEG";
		case DREM:
			return "DREM";
		case DRETURN:
			return "DRETURN";
		case DSTORE:
			return "DSTORE";
		case DSTORE0:
			return "DSTORE0";
		case DSTORE1:
			return "DSTORE1";
		case DSTORE2:
			return "DSTORE2";
		case DSTORE3:
			return "DSTORE3";
		case DSUB:
			return "DSUB";
		case DUP:
			return "DUP";
		case DUPI:
			return "DUPI";
		case DUPII:
			return "DUPII";
		case DUP8:
			return "DUP8";
		case DUP8I:
			return "DUP8I";
		case DUP8II:
			return "DUP8II";
		case F2D:
			return "F2D";
		case F2I:
			return "F2I";
		case F2L:
			return "F2L";
		case FADD:
			return "FADD";
		case FALOAD:
			return "FALOAD";
		case FASTORE:
			return "FASTORE";
		case FCMPG:
			return "FCMPG";
		case FCMPL:
			return "FCMPL";
		case FCONST0:
			return "FCONST0";
		case FCONST1:
			return "FCONST1";
		case FCONST2:
			return "FCONST2";
		case FDIV:
			return "FDIV";
		case FLOAD:
			return "FLOAD";
		case FLOAD0:
			return "FLOAD0";
		case FLOAD1:
			return "FLOAD1";
		case FLOAD2:
			return "FLOAD2";
		case FLOAD3:
			return "FLOAD3";
		case FMUL:
			return "FMUL";
		case FNEG:
			return "FNEG";
		case FREM:
			return "FREM";
		case FRETURN:
			return "FRETURN";
		case FSTORE:
			return "FSTORE";
		case FSTORE0:
			return "FSTORE0";
		case FSTORE1:
			return "FSTORE1";
		case FSTORE2:
			return "FSTORE2";
		case FSTORE3:
			return "FSTORE3";
		case FSUB:
			return "FSUB";
		case GETFIELD:
			return "GETFIELD";
		case GETSTATIC:
			return "GETSTATIC";
		case GOTO:
			return "GOTO";
		case GOTO4:
			return "GOTO4";
		case I2B:
			return "I2B";
		case I2C:
			return "I2C";
		case I2D:
			return "I2D";
		case I2F:
			return "I2F";
		case I2L:
			return "I2L";
		case I2S:
			return "I2S";
		case IADD:
			return "IADD";
		case IALOAD:
			return "IALOAD";
		case IAND:
			return "IAND";
		case IASTORE:
			return "IASTORE";
		case ICONSTm1:
			return "ICONSTm1";
		case ICONST0:
			return "ICONST0";
		case ICONST1:
			return "ICONST1";
		case ICONST2:
			return "ICONST2";
		case ICONST3:
			return "ICONST3";
		case ICONST4:
			return "ICONST4";
		case ICONST5:
			return "ICONST5";
		case IDIV:
			return "IDIV";
		case IFAE:
			return "IFAE";
		case IFAN:
			return "IFAN";
		case IFIE:
			return "IFIE";
		case IFIE0:
			return "IFIE0";
		case IFIG:
			return "IFIG";
		case IFIG0:
			return "IFIG0";
		case IFIGE:
			return "IFIGE";
		case IFIGE0:
			return "IFIGE0";
		case IFIL:
			return "IFIL";
		case IFIL0:
			return "IFIL0";
		case IFILE:
			return "IFILE";
		case IFILE0:
			return "IFILE0";
		case IFIN:
			return "IFIN";
		case IFIN0:
			return "IFIN0";
		case IFNOTNULL:
			return "IFNOTNULL";
		case IFNULL:
			return "IFNULL";
		case IINC:
			return "IINC";
		case ILOAD:
			return "ILOAD";
		case ILOAD0:
			return "ILOAD0";
		case ILOAD1:
			return "ILOAD1";
		case ILOAD2:
			return "ILOAD2";
		case ILOAD3:
			return "ILOAD3";
		case IMUL:
			return "IMUL";
		case INEG:
			return "INEG";
		case INSTANCEOF:
			return "INSTANCEOF";
		case INVOKEINTERFACE:
			return "INVOKEINTERFACE";
		case INVOKESPECIAL:
			return "INVOKESPECIAL";
		case INVOKESTATIC:
			return "INVOKESTATIC";
		case INVOKEVIRTUAL:
			return "INVOKEVIRTUAL";
		case IOR:
			return "IOR";
		case IREM:
			return "IREM";
		case IRETURN:
			return "IRETURN";
		case ISHL:
			return "ISHL";
		case ISHR:
			return "ISHR";
		case ISTORE:
			return "ISTORE";
		case ISTORE0:
			return "ISTORE0";
		case ISTORE1:
			return "ISTORE1";
		case ISTORE2:
			return "ISTORE2";
		case ISTORE3:
			return "ISTORE3";
		case ISUB:
			return "ISUB";
		case IUSHR:
			return "IUSHR";
		case IXOR:
			return "IXOR";
		case JSR:
			return "JSR";
		case JSR4:
			return "JSR4";
		case L2D:
			return "L2D";
		case L2F:
			return "L2F";
		case L2I:
			return "L2I";
		case LADD:
			return "LADD";
		case LALOAD:
			return "LALOAD";
		case LAND:
			return "LAND";
		case LASTORE:
			return "LASTORE";
		case LCMP:
			return "LCMP";
		case LCONST0:
			return "LCONST0";
		case LCONST1:
			return "LCONST1";
		case LDC:
			return "LDC";
		case LDCW:
			return "LDCW";
		case LDC8W:
			return "LDC8W";
		case LDIV:
			return "LDIV";
		case LLOAD:
			return "LLOAD";
		case LLOAD0:
			return "LLOAD0";
		case LLOAD1:
			return "LLOAD1";
		case LLOAD2:
			return "LLOAD2";
		case LLOAD3:
			return "LLOAD3";
		case LMUL:
			return "LMUL";
		case LNEG:
			return "LNEG";
		case LOOKUPSWITCH:
			return "LOOKUPSWITCH";
		case LOR:
			return "LOR";
		case LREM:
			return "LREM";
		case LRETURN:
			return "LRETURN";
		case LSHL:
			return "LSHL";
		case LSHR:
			return "LSHR";
		case LSTORE:
			return "LSTORE";
		case LSTORE0:
			return "LSTORE0";
		case LSTORE1:
			return "LSTORE1";
		case LSTORE2:
			return "LSTORE2";
		case LSTORE3:
			return "LSTORE3";
		case LSUB:
			return "LSUB";
		case LUSHR:
			return "LUSHR";
		case LXOR:
			return "LXOR";
		case MONITORENTER:
			return "MONITORENTER";
		case MONITOREXIT:
			return "MONITOREXIT";
		case MULTIANEWARRAY:
			return "MULTIANEWARRAY";
		case NEW:
			return "NEW";
		case NEWARRAY:
			return "NEWARRAY";
		case NOP:
			return "NOP";
		case POP:
			return "POP";
		case POP8:
			return "POP8";
		case PUTFIELD:
			return "PUTFIELD";
		case PUTSTATIC:
			return "PUTSTATIC";
		case RET:
			return "RET";
		case RETURN:
			return "RETURN";
		case SALOAD:
			return "SALOAD";
		case SASTORE:
			return "SASTORE";
		case SIPUSH:
			return "SIPUSH";
		case SWAP:
			return "SWAP";
		case TABLESWITCH:
			return "TABLESWITCH";
		case WIDE:
			return "WIDE";
		}
		throw new ClassFormatError("invalid opcode " + (op & 0xFF));
	}

	public static void println(Code c, int ad, PrintStream out, int indent1st, int indent,
		int verbose)
	{
		println(c.insBytes(), ad + c.getAddrBi(), c.getAddrBi(), c.cons, out, indent1st,
			indent, verbose);
	}

	public static void println(byte[] bs, int bi, int addrBi, Constants cons,
		PrintStream out, int indent1st, int indent, int verbose)
	{
		Element.printIndent(out, indent1st);
		out.print(toString(bs[bi]));
		int ad = bi - addrBi;
		switch (bs[bi])
		{
		case BIPUSH:
			out.print(' ');
			out.println(bs[bi + 1]);
			return;
		case SIPUSH:
			out.print(' ');
			out.println(Bytes.readS2(bs, bi + 1));
			return;
		case ALOAD:
		case ASTORE:
		case DLOAD:
		case DSTORE:
		case FLOAD:
		case FSTORE:
		case ILOAD:
		case ISTORE:
		case LLOAD:
		case LSTORE:
		case RET:
			out.print(" local ");
			out.println(Bytes.readU1(bs, bi + 1));
			return;
		case LDC:
			out.print(' ');
			Constants.print(cons, out, Bytes.readU1(bs, bi + 1), verbose);
			break;
		case NEWARRAY:
			out.print(' ');
			switch (bs[bi + 1])
			{
			case NEWARRAY_BOOL:
				out.println("bool");
				return;
			case NEWARRAY_CHAR:
				out.println("char");
				return;
			case NEWARRAY_FLOAT:
				out.println("float");
				return;
			case NEWARRAY_DOUBLE:
				out.println("double");
				return;
			case NEWARRAY_BYTE:
				out.println("byte");
				return;
			case NEWARRAY_SHORT:
				out.println("short");
				return;
			case NEWARRAY_INT:
				out.println("int");
				return;
			case NEWARRAY_LONG:
				out.println("long");
				return;
			}
			throw new ClassFormatError("invalid newarray type");
		case ANEWARRAY:
		case CHECKCAST:
		case GETFIELD:
		case GETSTATIC:
		case INSTANCEOF:
		case INVOKESPECIAL:
		case INVOKESTATIC:
		case INVOKEVIRTUAL:
		case LDCW:
		case LDC8W:
		case NEW:
		case PUTFIELD:
		case PUTSTATIC:
			out.print(' ');
			Constants.print(cons, out, Bytes.readU2(bs, bi + 1), verbose);
			break;
		case IINC:
			out.print(" local ");
			out.print(Bytes.readU1(bs, bi + 1));
			out.print(" by ");
			out.println(bs[bi + 2]);
			return;
		case INVOKEINTERFACE:
			out.print(' ');
			Constants.print(cons, out, Bytes.readU2(bs, bi + 1), verbose);
			out.print(" with ");
			out.print(Bytes.readU1(bs, bi + 3));
			out.println(" stacks");
			return;
		case GOTO:
		case IFAE:
		case IFAN:
		case IFIE:
		case IFIE0:
		case IFIG:
		case IFIG0:
		case IFIGE:
		case IFIGE0:
		case IFIL:
		case IFIL0:
		case IFILE:
		case IFILE0:
		case IFIN:
		case IFIN0:
		case IFNOTNULL:
		case IFNULL:
		case JSR:
			out.print(" addr ");
			out.println(ad + Bytes.readS2(bs, bi + 1));
			return;
		case MULTIANEWARRAY:
			out.print(' ');
			Constants.print(cons, out, Bytes.readU2(bs, bi + 1), verbose);
			out.print(" dimen ");
			out.println(Bytes.readU1(bs, bi + 3));
			return;
		case GOTO4:
		case JSR4:
			out.print(" addr ");
			out.println(ad + Bytes.readS4(bs, bi + 1));
			return;
		case WIDE:
			out.print(' ');
			out.print(toString(bs[bi + 1]));
			switch (bs[bi + 1])
			{
			case ALOAD:
			case ASTORE:
			case DLOAD:
			case DSTORE:
			case FLOAD:
			case FSTORE:
			case ILOAD:
			case ISTORE:
			case LLOAD:
			case LSTORE:
			case RET:
				out.print(" local ");
				out.println(Bytes.readU2(bs, bi + 1));
				return;
			case IINC:
				out.print(" local ");
				out.print(Bytes.readU2(bs, bi + 1));
				out.print(' ');
				out.print(" by ");
				out.println(Bytes.readS2(bs, bi + 3));
				return;
			}
		case LOOKUPSWITCH:
		{
			int h = 4 - (ad & 3);
			bi += h;
			out.print(" default goto ");
			out.print(ad + Bytes.readS4(bs, bi));
			int n = Bytes.readS4(bs, bi + 4);
			if (n >> 16 != 0)
				throw new ClassFormatError("invalid lookup switch valueN " + n);
			if (verbose > 0)
			{
				bi += 8;
				for (int i = 0; i < n; i++, bi += 8)
				{
					out.println();
					Element.printIndent(out, indent);
					out.print(i);
					out.print(". ");
					out.print(Bytes.readS4(bs, bi));
					out.print(" goto ");
					out.print(ad + Bytes.readS4(bs, bi + 4));
				}
			}
			else
			{
				out.print(" valueN ");
				out.print(n);
			}
			break;
		}
		case TABLESWITCH:
		{
			int h = 4 - (ad & 3);
			bi += h;
			out.print(" default goto ");
			out.print(ad + Bytes.readS4(bs, bi));
			int low = Bytes.readS4(bs, bi + 4);
			int high = Bytes.readS4(bs, bi + 8);
			int n = high - low + 1;
			if (n >> 16 != 0)
				throw new ClassFormatError("invalid table switch low/high value " + low + ' '
					+ high);
			if (verbose > 0)
			{
				bi += 12;
				for (int i = 0; i < n; i++, bi += 4)
				{
					out.println();
					Element.printIndent(out, indent);
					out.print(low + i);
					out.print(". goto ");
					out.print(ad + Bytes.readS4(bs, bi));
				}
			}
			else
			{
				out.print(" low ");
				out.print(low);
				out.print(" high ");
				out.print(high);
			}
			break;
		}
		}
		out.println();
	}

	public static int getInsAddrN(Code c, int ad)
	{
		return getInsAddrN(c.insBytes(), ad + c.getAddrBi(), c.getAddrBi());
	}

	public static int getInsAddrN(byte[] bs, int bi, int addrBi)
	{
		switch (bs[bi])
		{
		case AALOAD:
		case AASTORE:
		case ACONSTNULL:
		case ALOAD0:
		case ALOAD1:
		case ALOAD2:
		case ALOAD3:
		case ARETURN:
		case ARRAYLENGTH:
		case ASTORE0:
		case ASTORE1:
		case ASTORE2:
		case ASTORE3:
		case ATHROW:
		case BALOAD:
		case BASTORE:
		case CALOAD:
		case CASTORE:
		case D2F:
		case D2I:
		case D2L:
		case DADD:
		case DALOAD:
		case DASTORE:
		case DCMPG:
		case DCMPL:
		case DCONST0:
		case DCONST1:
		case DDIV:
		case DLOAD0:
		case DLOAD1:
		case DLOAD2:
		case DLOAD3:
		case DMUL:
		case DNEG:
		case DREM:
		case DRETURN:
		case DSTORE0:
		case DSTORE1:
		case DSTORE2:
		case DSTORE3:
		case DSUB:
		case DUP:
		case DUPI:
		case DUPII:
		case DUP8:
		case DUP8I:
		case DUP8II:
		case F2D:
		case F2I:
		case F2L:
		case FADD:
		case FALOAD:
		case FASTORE:
		case FCMPG:
		case FCMPL:
		case FCONST0:
		case FCONST1:
		case FCONST2:
		case FDIV:
		case FLOAD0:
		case FLOAD1:
		case FLOAD2:
		case FLOAD3:
		case FMUL:
		case FNEG:
		case FREM:
		case FRETURN:
		case FSTORE0:
		case FSTORE1:
		case FSTORE2:
		case FSTORE3:
		case FSUB:
		case I2B:
		case I2C:
		case I2D:
		case I2F:
		case I2L:
		case I2S:
		case IADD:
		case IALOAD:
		case IAND:
		case IASTORE:
		case ICONSTm1:
		case ICONST0:
		case ICONST1:
		case ICONST2:
		case ICONST3:
		case ICONST4:
		case ICONST5:
		case IDIV:
		case ILOAD0:
		case ILOAD1:
		case ILOAD2:
		case ILOAD3:
		case IMUL:
		case INEG:
		case IOR:
		case IREM:
		case IRETURN:
		case ISHL:
		case ISHR:
		case ISTORE0:
		case ISTORE1:
		case ISTORE2:
		case ISTORE3:
		case ISUB:
		case IUSHR:
		case IXOR:
		case L2D:
		case L2F:
		case L2I:
		case LADD:
		case LALOAD:
		case LAND:
		case LASTORE:
		case LCMP:
		case LCONST0:
		case LCONST1:
		case LDIV:
		case LLOAD0:
		case LLOAD1:
		case LLOAD2:
		case LLOAD3:
		case LMUL:
		case LNEG:
		case LOR:
		case LREM:
		case LRETURN:
		case LSHL:
		case LSHR:
		case LSTORE0:
		case LSTORE1:
		case LSTORE2:
		case LSTORE3:
		case LSUB:
		case LUSHR:
		case LXOR:
		case MONITORENTER:
		case MONITOREXIT:
		case NOP:
		case POP:
		case POP8:
		case RETURN:
		case SALOAD:
		case SASTORE:
		case SWAP:
			return 1;
		case ALOAD:
		case ASTORE:
		case BIPUSH:
		case DLOAD:
		case DSTORE:
		case FLOAD:
		case FSTORE:
		case ILOAD:
		case ISTORE:
		case LDC:
		case LLOAD:
		case LSTORE:
		case NEWARRAY:
		case RET:
			return 2;
		case ANEWARRAY:
		case CHECKCAST:
		case GETFIELD:
		case GETSTATIC:
		case GOTO:
		case IFAE:
		case IFAN:
		case IFIE:
		case IFIE0:
		case IFIG:
		case IFIG0:
		case IFIGE:
		case IFIGE0:
		case IFIL:
		case IFIL0:
		case IFILE:
		case IFILE0:
		case IFIN:
		case IFIN0:
		case IFNOTNULL:
		case IFNULL:
		case IINC:
		case INSTANCEOF:
		case INVOKESPECIAL:
		case INVOKESTATIC:
		case INVOKEVIRTUAL:
		case JSR:
		case LDCW:
		case LDC8W:
		case NEW:
		case PUTFIELD:
		case PUTSTATIC:
		case SIPUSH:
			return 3;
		case MULTIANEWARRAY:
			return 4;
		case GOTO4:
		case INVOKEINTERFACE:
		case JSR4:
			return 5;
		case WIDE:
			switch (bs[bi + 1])
			{
			case ALOAD:
			case ASTORE:
			case DLOAD:
			case DSTORE:
			case FLOAD:
			case FSTORE:
			case ILOAD:
			case ISTORE:
			case LLOAD:
			case LSTORE:
			case RET:
				return 4;
			case IINC:
				return 6;
			}
		case LOOKUPSWITCH:
		{
			int h = 4 - (bi - addrBi & 3);
			int n = Bytes.readU4(bs, bi + h + 4);
			if (n >> 16 != 0)
				throw new ClassFormatError("invalid lookup switch valueN " + n);
			return h + 8 + (n << 3);
		}
		case TABLESWITCH:
		{
			int h = 4 - (bi - addrBi & 3);
			int low = Bytes.readS4(bs, bi + h + 4);
			int high = Bytes.readS4(bs, bi + h + 8);
			int n = high - low + 1;
			if (n >> 16 != 0)
				throw new ClassFormatError("invalid table switch low/high value " + low + ' '
					+ high);
			return h + 12 + (n << 2);
		}
		}
		throw new ClassFormatError("invalid opcode " + (bs[bi] & 0xFF));
	}

	public static byte getLoadOp(char typeChar)
	{
		switch (typeChar)
		{
		case 'L':
		case '[':
			return ALOAD;
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
			return ILOAD;
		case 'J':
			return LLOAD;
		case 'F':
			return FLOAD;
		case 'D':
			return DLOAD;
		}
		throw new IllegalArgumentException("invalid type char " + typeChar);
	}

	public static byte getStoreOp(char typeChar)
	{
		switch (typeChar)
		{
		case 'L':
		case '[':
			return ASTORE;
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
			return ISTORE;
		case 'J':
			return LSTORE;
		case 'F':
			return FSTORE;
		case 'D':
			return DSTORE;
		}
		throw new IllegalArgumentException("invalid type char " + typeChar);
	}

	public static byte getReturnOp(char typeChar)
	{
		switch (typeChar)
		{
		case 'L':
		case '[':
			return ARETURN;
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
			return IRETURN;
		case 'J':
			return LRETURN;
		case 'F':
			return FRETURN;
		case 'D':
			return DRETURN;
		case 'V':
			return RETURN;
		}
		throw new IllegalArgumentException("invalid type char " + typeChar);
	}

	public static byte getDefaultConstOp(char typeChar)
	{
		switch (typeChar)
		{
		case 'L':
		case '[':
			return ACONSTNULL;
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
			return ICONST0;
		case 'J':
			return LCONST0;
		case 'F':
			return FCONST0;
		case 'D':
			return DCONST0;
		}
		throw new IllegalArgumentException("invalid type char " + typeChar);
	}

	public static int getLocalStackN(char typeChar)
	{
		switch (typeChar)
		{
		case 'L':
		case '[':
		case 'Z':
		case 'B':
		case 'C':
		case 'S':
		case 'I':
		case 'F':
			return 1;
		case 'J':
		case 'D':
			return 2;
		case 'V':
			return 0;
		}
		throw new ClassFormatError("invalid type char " + typeChar);
	}

	public static byte getNormalLocalOp(Code c, int ad)
	{
		return getNormalLocalOp(c.insBytes(), ad + c.getAddrBi());
	}

	public static byte getNormalLocalOp(byte[] bs, int bi)
	{
		switch (bs[bi])
		{
		case ALOAD0:
		case ALOAD1:
		case ALOAD2:
		case ALOAD3:
			return ALOAD;
		case ILOAD0:
		case ILOAD1:
		case ILOAD2:
		case ILOAD3:
			return ILOAD;
		case LLOAD0:
		case LLOAD1:
		case LLOAD2:
		case LLOAD3:
			return LLOAD;
		case FLOAD0:
		case FLOAD1:
		case FLOAD2:
		case FLOAD3:
			return FLOAD;
		case DLOAD0:
		case DLOAD1:
		case DLOAD2:
		case DLOAD3:
			return DLOAD;
		case ASTORE0:
		case ASTORE1:
		case ASTORE2:
		case ASTORE3:
			return ASTORE;
		case ISTORE0:
		case ISTORE1:
		case ISTORE2:
		case ISTORE3:
			return ISTORE;
		case LSTORE0:
		case LSTORE1:
		case LSTORE2:
		case LSTORE3:
			return LSTORE;
		case FSTORE0:
		case FSTORE1:
		case FSTORE2:
		case FSTORE3:
			return FSTORE;
		case DSTORE0:
		case DSTORE1:
		case DSTORE2:
		case DSTORE3:
			return DSTORE;
		case WIDE:
			return bs[bi + 1];
		default:
			return bs[bi];
		}
	}

	public static int getLocalIndex(Code c, int ad)
	{
		return getLocalIndex(c.insBytes(), ad + c.getAddrBi());
	}

	public static int getLocalIndex(byte[] bs, int bi)
	{
		switch (bs[bi])
		{
		case ALOAD0:
		case ILOAD0:
		case LLOAD0:
		case FLOAD0:
		case DLOAD0:
		case ASTORE0:
		case ISTORE0:
		case LSTORE0:
		case FSTORE0:
		case DSTORE0:
			return 0;
		case ALOAD1:
		case ILOAD1:
		case LLOAD1:
		case FLOAD1:
		case DLOAD1:
		case ASTORE1:
		case ISTORE1:
		case LSTORE1:
		case FSTORE1:
		case DSTORE1:
			return 1;
		case ALOAD2:
		case ILOAD2:
		case LLOAD2:
		case FLOAD2:
		case DLOAD2:
		case ASTORE2:
		case ISTORE2:
		case LSTORE2:
		case FSTORE2:
		case DSTORE2:
			return 2;
		case ALOAD3:
		case ILOAD3:
		case LLOAD3:
		case FLOAD3:
		case DLOAD3:
		case ASTORE3:
		case ISTORE3:
		case LSTORE3:
		case FSTORE3:
		case DSTORE3:
			return 3;
		case ALOAD:
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
		case ASTORE:
		case ISTORE:
		case LSTORE:
		case FSTORE:
		case DSTORE:
		case IINC:
		case RET:
			return Bytes.readU1(bs, bi + 1);
		case WIDE:
			switch (bs[bi + 1])
			{
			case ALOAD:
			case ILOAD:
			case LLOAD:
			case FLOAD:
			case DLOAD:
			case ASTORE:
			case ISTORE:
			case LSTORE:
			case FSTORE:
			case DSTORE:
			case IINC:
			case RET:
				return Bytes.readU2(bs, bi + 2);
			}
			throw new ClassFormatError("invalid opcode " + (bs[bi + 1] & 0xFF));
		}
		throw new IllegalArgumentException("invalid opcode " + (bs[bi] & 0xFF));
	}
}
