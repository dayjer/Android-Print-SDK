/*****************************************************
 *
 * MultipleUnitSize.java
 *
 *
 * Modified MIT License
 *
 * Copyright (c) 2010-2015 Kite Tech Ltd. https://www.kite.ly
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The software MAY ONLY be used with the Kite Tech Ltd platform and MAY NOT be modified
 * to be used with any competitor platforms. This means the software MAY NOT be modified 
 * to place orders with any competitors to Kite Tech Ltd, all orders MUST go through the
 * Kite Tech Ltd platform servers. 
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 *****************************************************/

///// Package Declaration /////

package ly.kite.print;


///// Import(s) /////


///// Class Declaration /////

import java.util.Currency;
import java.util.HashMap;
import java.util.Set;

import ly.kite.print.SingleUnitSize;
import ly.kite.print.UnitOfLength;

/*****************************************************
 *
 * This class represents a size in multiple units.
 *
 *****************************************************/
public class MultipleUnitSize
  {
  ////////// Static Constant(s) //////////

  @SuppressWarnings( "unused" )
  private static final String  LOG_TAG = "MultipleUnitSize";


  ////////// Static Variable(s) //////////


  ////////// Member Variable(s) //////////

  private HashMap<UnitOfLength,SingleUnitSize>  mUnitSizeTable;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////


  ////////// Constructor(s) //////////

  public MultipleUnitSize()
    {
    mUnitSizeTable = new HashMap<>();
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Adds a size in a single unit.
   *
   *****************************************************/
  public void add( SingleUnitSize singleUnitSize )
    {
    mUnitSizeTable.put( singleUnitSize.getUnit(), singleUnitSize );
    }


  /*****************************************************
   *
   * Returns the size for a unit.
   *
   *****************************************************/
  public SingleUnitSize get( UnitOfLength unit )
    {
    return ( mUnitSizeTable.get( unit ) );
    }


  /*****************************************************
   *
   * Returns the size at a position.
   *
   *****************************************************/
  public SingleUnitSize get( int position )
    {
    return ( mUnitSizeTable.get( position ) );
    }


  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * ...
   *
   *****************************************************/

  }
