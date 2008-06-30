//
// Copyright 2007-2008 Qianyan Cai
// Under the terms of the GNU Library General Public License version 2
//
package objot.bytecode;

import java.io.PrintStream;

import objot.util.Array2;
import objot.util.Class2;
import objot.util.InvalidValueException;


public final class Annotations
	extends Element
{
	public final Constants cons;
	public final boolean hided;
	int annoN;
	Annotation[] annos;

	public Annotations(Constants c, byte[] bs, int beginBi_, boolean hided_)
	{
		super(bs, beginBi_);
		cons = c;
		hided = hided_;
		annoN = read0u2(beginBi + 6);
		int bi = beginBi + 8;
		for (int i = 0; i < annoN; i++)
			bi += Annotation.readByteN(bytes, bi);
		if (bi - beginBi - 6 != read0u4(beginBi + 2))
			throw new ClassFormatError("inconsistent attribute length");
		end1Bi = bi;
	}

	public int getAnnoN()
	{
		return annoN;
	}

	void checkIndex(int ai)
	{
		if (ai < 0 || ai >= annoN)
			throw new InvalidValueException(ai);
	}

	void readAnnos()
	{
		if (annos != null)
			return;
		annos = new Annotation[allocN(annoN)];
		int bi = beginBi + 8;
		for (int i = 0; i < annoN; i++)
		{
			annos[i] = new Annotation(cons, bytes, bi);
			bi = annos[i].end1Bi;
		}
	}

	public Annotation getAnno(int ai)
	{
		checkIndex(ai);
		readAnnos();
		return annos[ai];
	}

	/** @return the index of annotation found, negative for not found. */
	public static int searchAnno(Annotations as,
		Class<? extends java.lang.annotation.Annotation> anno)
	{
		if (as != null)
			for (int i = as.annoN - 1; i >= 0; i--)
				if (as.cons.equalsUtf(as.getAnno(i).getDescCi(), utf(Class2.descript(anno))))
					return i;
		return -1;
	}

	/** @return the annotation found, null for not found. */
	public static Annotation searchAnno(Element a,
		Class<? extends java.lang.annotation.Annotation> anno)
	{
		int i = searchAnno(a.getAnnos(), anno);
		if (i >= 0)
			return a.getAnnos().getAnno(i);
		i = searchAnno(a.getAnnoHides(), anno);
		if (i >= 0)
			return a.getAnnoHides().getAnno(i);
		return null;
	}

	/** @return the index of annotated annotation found, negative for not found. */
	public static int searchAnnoAnno(ClassLoader cl, Annotations as,
		Class<? extends java.lang.annotation.Annotation> anno) throws ClassNotFoundException
	{
		if (as != null)
			for (int i = as.annoN - 1; i >= 0; i--)
			{
				int desc = as.getAnno(i).getDescCi();
				Class<?> ca = cl.loadClass(as.cons.classDesc2NameUcs(desc));
				if (ca.isAnnotationPresent(anno))
					return i;
			}
		return -1;
	}

	/** @return the annotated annotation found, null for not found. */
	public static Annotation searchAnnoAnno(ClassLoader cl, Element a,
		Class<? extends java.lang.annotation.Annotation> anno) throws ClassNotFoundException
	{
		int i = searchAnnoAnno(cl, a.getAnnos(), anno);
		if (i >= 0)
			return a.getAnnos().getAnno(i);
		i = searchAnnoAnno(cl, a.getAnnoHides(), anno);
		if (i >= 0)
			return a.getAnnoHides().getAnno(i);
		return null;
	}

	@Override
	void printContents(PrintStream out, int indent1st, int indent, int verbose)
	{
		if (verbose > 0)
		{
			printIndent(out, indent1st);
			out.print(" annoN ");
			out.print(annoN);
		}
		out.println();
		for (int i = 0; i < annoN; i++)
		{
			printIndent(out, indent);
			out.print(i);
			out.print('.');
			getAnno(i).printTo(out, 0, indent, verbose);
		}
	}

	public void ensureAnnoN(int n)
	{
		readAnnos();
		annos = Array2.ensureN(annos, n);
	}

	public int addAnno(Annotation a)
	{
		if (cons != a.cons)
			throw new IllegalArgumentException("inconsistent constants");
		readAnnos();
		ensureAnnoN(annoN + 1);
		annos[annoN] = a;
		return annoN++;
	}

	public void setAnno(int ai, Annotation a)
	{
		if (cons != a.cons)
			throw new IllegalArgumentException("inconsistent constants");
		checkIndex(ai);
		readAnnos();
		annos[ai] = a;
	}

	@Override
	public int normalizeByteN()
	{
		if (annos == null)
			return byteN0();
		int n = 8;
		for (int i = 0; i < annoN; i++)
			n += annos[i].normalizeByteN();
		return n;
	}

	@Override
	public int normalizeTo(byte[] bs, int begin)
	{
		if (annos == null)
		{
			System.arraycopy(bytes, beginBi, bs, begin, byteN0());
			return begin + byteN0();
		}
		writeU2(bs, begin, read0u2(beginBi));
		writeU2(bs, begin + 6, annoN);
		int bi = begin + 8;
		for (int i = 0; i < annoN; i++)
			bi = annos[i].normalizeTo(bs, bi);
		writeS4(bs, begin + 2, bi - begin - 6);
		return bi;
	}
}
