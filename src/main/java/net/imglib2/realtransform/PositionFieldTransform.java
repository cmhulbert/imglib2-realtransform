/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2020 Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Grant Harris, Stefan Helfrich, Mark Hiner,
 * Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey, Melissa Linkert,
 * Mark Longair, Brian Northan, Nick Perry, Curtis Rueden, Johannes Schindelin,
 * Jean-Yves Tinevez and Michael Zinsmaier.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imglib2.realtransform;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPositionable;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import net.imglib2.view.composite.CompositeIntervalView;
import net.imglib2.view.composite.RealComposite;

/**
 * A {@link RealTransform} by continuous coordinate lookup.
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 * @author Caleb Hulbert &lt;hulbertc@janelia.hhmi.org&gt;
 * @author John Bogovic &lt;bogovicj@janelia.hhmi.org&gt;
 */
public class PositionFieldTransform implements RealTransform
{
	protected final RealRandomAccess< ? extends RealLocalizable > access;

	protected final int numTargetDimensions;

	public PositionFieldTransform( final RealRandomAccess< ? extends RealLocalizable > positionAccesses )
	{
		access = positionAccesses;
		numTargetDimensions = access.get().numDimensions();
	}

	public PositionFieldTransform( final RealRandomAccessible< ? extends RealLocalizable > positions )
	{
		access = positions.realRandomAccess();
		numTargetDimensions = access.get().numDimensions();
	}

	/**
	 *
	 * @param positions
	 * 			interleaved target coordinate, this means that the components
	 * 			of the target coordinates are in the 0th dimension
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions )
	{
		this( convertToComposite( positions ) );
	}

	/**
	 *
	 * @param positions
	 * 			interleaved target coordinate, this means that the components
	 * 			of the target coordinates are in the 0th dimension
	 * @param pixelToPhysical
	 * 			a transformation from pixel coordinates to physical coordinates
	 */
	public < T extends RealType< T > > PositionFieldTransform( final RandomAccessibleInterval< T > positions, final AffineGet pixelToPhysical )
	{
		this( RealViews.affine( convertToComposite( positions ), pixelToPhysical ) );
	}

	@Override
	public int numSourceDimensions()
	{
		return access.numDimensions();
	}

	@Override
	public int numTargetDimensions()
	{
		return numTargetDimensions;
	}

	@Override
	public void apply( final double[] source, final double[] target )
	{
		access.setPositionAndGet( source ).localize( target );
	}

	@Override
	public void apply( final float[] source, final float[] target )
	{
		access.setPositionAndGet( source ).localize( target );
	}

	@Override
	public void apply( final RealLocalizable source, final RealPositionable target )
	{
		access.setPositionAndGet( source ).localize( target );
	}

	@Override
	public RealTransform copy()
	{
		return new PositionFieldTransform( access.copy() );
	}

	private static < T extends RealType< T > > RealRandomAccessible< ? extends RealLocalizable > convertToComposite(
			final RandomAccessibleInterval< T > position )
	{
		final CompositeIntervalView< T, RealComposite< T > > collapsedFirst =
				Views.collapseReal(
						Views.moveAxis( position, 0, position.numDimensions() - 1 ) );

		return Views.interpolate(
				Views.extendBorder( collapsedFirst ),
				new NLinearInterpolatorFactory<>() );
	}
}
