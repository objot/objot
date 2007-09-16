package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.InvalidValueException;


public class AnnoParams
	extends Element
{
	public final Constants cons;
	public final boolean hided;
	protected int paramN;
	/** [parameter index] */
	protected int[] annoNs;
	/** [parameter index][annotation index] */
	protected Annotation[][] annos;

	public AnnoParams(Constants c, byte[] bs, int beginBi_, boolean hided_)
	{
		super(bs, beginBi_, true);
		cons = c;
		hided = hided_;
		paramN = read0u1(beginBi + 6);
		annoNs = Array2.newInts(paramN);
		int bi = beginBi + 7;
		for (int i = 0; i < paramN; i++)
		{
			annoNs[i] = read0u2(bi);
			bi += 2;
			for (int n = annoNs[i]; n > 0; n--)
				bi += Annotation.readByteN(bytes, bi);
		}
		if (bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
		end1Bi = bi;
	}

	public int getParamN()
	{
		return paramN;
	}

	protected void checkIndex(int pi)
	{
		if (pi < 0 || pi >= paramN)
			throw new InvalidValueException(pi);
	}

	public int getAnnoN(int pi)
	{
		checkIndex(pi);
		return annoNs[pi];
	}

	protected void checkIndex(int pi, int ai)
	{
		checkIndex(pi);
		if (ai < 0 || ai >= annoNs[pi])
			throw new InvalidValueException(pi);
	}

	protected void readAnnos()
	{
		if (annos != null)
			return;
		annos = new Annotation[paramN][];
		int bi = beginBi + 7;
		for (int i = 0; i < paramN; i++)
		{
			annos[i] = new Annotation[allocN(annoNs[i])];
			bi += 2;
			for (int j = 0; j < annoNs[i]; j++)
			{
				annos[i][j] = new Annotation(cons, bytes, bi);
				bi = annos[i][j].end1Bi;
			}
		}
	}

	public Annotation getAnno(int pi, int ai)
	{
		checkIndex(pi, ai);
		readAnnos();
		return annos[pi][ai];
	}

	/**
	 * @return the index(<code>arg << 32L | anno & 0xFFFFFFFFL</code>) of annotation
	 *         found, negative for not found.
	 */
	public static long searchAnno(AnnoParams as,
		Class<? extends java.lang.annotation.Annotation> anno)
	{
		if (as != null)
			for (int g = as.getParamN() - 1; g >= 0; g--)
				for (int a = as.getAnnoN(g) - 1; a >= 0; a--)
					if (as.cons.equalsUtf(as.getAnno(g, a).getDescCi(), utf(Class2
						.descriptor(anno))))
						return g << 32L | a & 0xFFFFFFFFL;
		return -1;
	}

	/**
	 * @return the index(<code>arg << 32L | anno & 0xFFFFFFFFL</code>) of annotated
	 *         annotation found, negative for not found.
	 */
	public static long searchAnnoAnno(ClassLoader cl, AnnoParams as,
		Class<? extends java.lang.annotation.Annotation> anno) throws ClassNotFoundException
	{
		if (as != null)
			for (int g = as.getParamN() - 1; g >= 0; g--)
				for (int a = as.getAnnoN(g) - 1; a >= 0; a--)
				{
					int desc = as.getAnno(g, a).getDescCi();
					Class<?> ca = cl.loadClass(as.cons.classDesc2InternalChars(desc).replace(
						'/', '.'));
					if (ca.isAnnotationPresent(anno))
						return g << 32L | a & 0xFFFFFFFFL;
				}
		return -1;
	}

	@Override
	protected void printContents(PrintStream out, int indent1st, int indent, int verbose,
		boolean hash)
	{
		out.println();
		for (int i = 0; i < getParamN(); i++)
			for (int j = 0; j < getAnnoN(i); i++)
			{
				printIndent(out, indent);
				out.print(i);
				out.print(',');
				out.println(j);
				out.print('.');
				getAnno(i, j).printTo(out, 0, indent, verbose, hash);
			}
	}

	public void ensureAnnoN(int pi, int an)
	{
		checkIndex(pi);
		readAnnos();
		annos[pi] = Array2.ensureN(annos[pi], an);
	}

	public int appendAnno(int pi, Annotation a)
	{
		checkIndex(pi);
		readAnnos();
		ensureAnnoN(pi, annoNs[pi] + 1);
		annos[pi][annoNs[pi]] = a;
		return annoNs[pi]++;
	}

	public void setAnno(int pi, int ai, Annotation a)
	{
		checkIndex(pi, ai);
		readAnnos();
		annos[pi][ai] = a;
	}

	@Override
	public int generateByteN()
	{
		if (annos == null)
			return byteN();
		int n = 7;
		for (int i = 0; i < paramN; i++)
		{
			n += 2;
			for (int j = 0; j < annoNs[i]; j++)
				n += annos[i][j].byteN();
		}
		return n;
	}

	@Override
	public int generateTo(byte[] bs, int begin)
	{
		if (annos == null)
		{
			System.arraycopy(bytes, beginBi, bs, begin, byteN());
			return begin + byteN();
		}
		writeU2(bs, begin, read0u2(beginBi));
		writeU1(bs, begin + 6, paramN);
		int bi = begin + 7;
		for (int gi = 0; gi < paramN; gi++)
		{
			writeU2(bs, bi, annoNs[gi]);
			bi += 2;
			for (int ai = 0; ai < annoNs[gi]; ai++)
			{
				Annotation a = annos[gi][ai];
				System.arraycopy(a.bytes, a.beginBi, bs, bi, a.byteN());
				bi += a.byteN();
			}
		}
		writeS4(bs, begin + 2, bi - begin - 6);
		return bi;
	}
}
