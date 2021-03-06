/*****************************************************
 *
 * UserJourneyType.java
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

package ly.kite.journey;


///// Import(s) /////

import ly.kite.R;


///// Class Declaration /////

/*****************************************************
 *
 * This enum defines a type of user journey through the
 * shopping process.
 *
 *****************************************************/
public enum UserJourneyType
  {
  CIRCLE        ( R.drawable.filled_white_circle ),
  FRAME,
  GREETINGCARD,
  PHONE_CASE    ( true ),
  PHOTOBOOK     ( R.drawable.filled_white_rectangle ),
  POSTCARD,
  POSTER,
  RECTANGLE     ( R.drawable.filled_white_rectangle );


  ////////// Member Variable(s) //////////

  private int      mMaskResourceId;
  private boolean  mUsesSingleImage;


  ////////// Static Initialiser(s) //////////


  ////////// Static Method(s) //////////


  ////////// Constructor(s) //////////

  private UserJourneyType( boolean usesSingleImage, int maskResourceId )
    {
    mUsesSingleImage = usesSingleImage;
    mMaskResourceId  = maskResourceId;
    }

  private UserJourneyType( boolean usesSingleImage )
    {
    this( usesSingleImage, 0 );
    }

  private UserJourneyType( int maskResourceId )
    {
    this( false, maskResourceId );
    }

  private UserJourneyType()
    {
    this( false, 0 );
    }


  ////////// Method(s) //////////

  /*****************************************************
   *
   * Returns the resource id of the mask.
   *
   *****************************************************/
  public int maskResourceId()
    {
    return ( mMaskResourceId );
    }


  /*****************************************************
   *
   * Returns true if the user journey type uses a single
   * image for creating items, false otherwise.
   *
   *****************************************************/
  public boolean usesSingleImage()
    {
    return ( mUsesSingleImage );
    }




  ////////// Inner Class(es) //////////

  /*****************************************************
   *
   * ...
   *
   *****************************************************/

  }

