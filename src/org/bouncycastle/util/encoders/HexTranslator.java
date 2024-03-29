// Copyright (c) 2000 - 2009 The Legion Of The Bouncy Castle (http://www.bouncycastle.org)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to
// deal in the Software without restriction, including without limitation the
// rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
// IN THE SOFTWARE.

package org.bouncycastle.util.encoders;

/**
 * Converters for going from hex to binary and back. Note: this class assumes ASCII processing.
 */
public class HexTranslator
    implements Translator
{
    private static final byte[]   hexTable = 
        { 
            (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
            (byte)'8', (byte)'9', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f'
        };

    /**
     * size of the output block on encoding produced by getDecodedBlockSize()
     * bytes.
     */
    public int getEncodedBlockSize()
    {
        return 2;
    }

    public int encode(
        byte[]  in,
        int     inOff,
        int     length,
        byte[]  out,
        int     outOff)
    {
        for (int i = 0, j = 0; i < length; i++, j += 2)
        {
            out[outOff + j] = hexTable[(in[inOff] >> 4) & 0x0f];
            out[outOff + j + 1] = hexTable[in[inOff] & 0x0f];

            inOff++;
        }

        return length * 2;
    }

    /**
     * size of the output block on decoding produced by getEncodedBlockSize()
     * bytes.
     */
    public int getDecodedBlockSize()
    {
        return 1;
    }

    public int decode(
        byte[]  in,
        int     inOff,
        int     length,
        byte[]  out,
        int     outOff)
    {
        int halfLength = length / 2;
        byte left, right;
        for (int i = 0; i < halfLength; i++)
        {
            left  = in[inOff + i * 2];
            right = in[inOff + i * 2 + 1];
            
            if (left < (byte)'a')
            {
                out[outOff] = (byte)((left - '0') << 4);
            }
            else
            {
                out[outOff] = (byte)((left - 'a' + 10) << 4);
            }
            if (right < (byte)'a')
            {
                out[outOff] += (byte)(right - '0');
            }
            else
            {
                out[outOff] += (byte)(right - 'a' + 10);
            }

            outOff++;
        }

        return halfLength;
    }
}
