/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Author: Roger Martinez - Red Hat
 */

package com.ait.lienzo.client.core.shape;

import com.ait.lienzo.client.core.shape.storage.IStorageEngine;
import com.ait.lienzo.client.core.shape.storage.PrimitiveFastArrayStorageEngine;
import com.ait.lienzo.client.core.types.BoundingBox;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.client.core.types.Shadow;
import com.ait.lienzo.shared.core.types.ColorName;
import com.ait.lienzo.shared.core.types.GroupType;
import com.ait.lienzo.shared.core.types.IColor;
import com.ait.lienzo.shared.core.types.TextAlign;
import com.ait.lienzo.shared.core.types.TextBaseLine;

public class ToolTip extends GroupOf<IPrimitive<?>, ToolTip>
{
    public static final double  TRIANGLE_SIZE          = 10;

    private static final double TOOLTIP_PADDING_WIDTH  = 25;

    private static final double TOOLTIP_PADDING_HEIGHT = 25;

    private static final IColor TOOLTIP_COLOR          = ColorName.WHITESMOKE;

    private static final String FONT_FAMILY            = "Verdana";

    private static final String TEXT_FONT_STYLE        = "normal";

    private static final String LABL_FONT_STYLE        = "bold";

    private static final int    FONT_SIZE              = 10;

    private static final IColor LABEL_COLOR            = ColorName.BLACK;

    private final Rectangle     m_body;

    private final Triangle      m_tail;

    private final Triangle      m_mask;

    private final Text          m_text;

    private final Text          m_labl;

    private Layer               m_draw;

    private boolean             m_show;

    private double              m_oldx;

    private double              m_oldy;

    private String              m_textValue            = "";

    private String              m_lablValue            = "";

    private static final Shadow SHADOW                 = new Shadow(ColorName.BLACK.getColor().setA(0.80), 10, 3, 3);

    public ToolTip()
    {
        this(null);
    }

    public ToolTip(final Layer layer)
    {
        super(GroupType.TOOLTIP, new PrimitiveFastArrayStorageEngine());

        m_body = new Rectangle(1, 1).setFillColor(TOOLTIP_COLOR).setCornerRadius(5).setStrokeWidth(1).setShadow(SHADOW);

        m_tail = new Triangle(new Point2D(1, 1), new Point2D(1, 1), new Point2D(1, 1)).setFillColor(TOOLTIP_COLOR).setStrokeWidth(1).setShadow(SHADOW);

        m_mask = new Triangle(new Point2D(1, 1), new Point2D(1, 1), new Point2D(1, 1)).setFillColor(TOOLTIP_COLOR);

        m_text = new Text("", FONT_FAMILY, TEXT_FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.MIDDLE);

        m_labl = new Text("", FONT_FAMILY, LABL_FONT_STYLE, FONT_SIZE).setFillColor(LABEL_COLOR).setTextAlign(TextAlign.LEFT).setTextBaseLine(TextBaseLine.MIDDLE);

        add(m_body);

        add(m_tail);

        add(m_mask);

        add(m_text);

        add(m_labl);

        setListening(false);

        m_show = true;

        setLayer(layer);
    }

    public ToolTip setLayer(final Layer layer)
    {
        if (layer != m_draw)
        {
            if ((null != m_draw) && (false == m_show))
            {
                m_draw.remove(this);

                m_draw.batch();
            }
            m_draw = layer;

            if ((null != m_draw) && (false == m_show))
            {
                m_draw.add(this);

                m_draw.batch();
            }
        }
        return this;
    }

    public ToolTip show(final double x, final double y)
    {
        return show(x, y, false);
    }

    public ToolTip show(final double x, final double y, final boolean force)
    {
        if (null == m_draw)
        {
            return this;
        }
        if ((false == force) && (false == m_show))
        {
            return this;
        }
        if (false == m_show)
        {
            m_draw.remove(this);

            m_draw.batch();
        }
        m_oldx = x;

        m_oldy = y;

        m_show = false;

        m_text.setText(m_textValue);

        BoundingBox bb = m_text.getBoundingBox();

        final double ctw = bb.getWidth();

        final double cth = bb.getHeight();

        m_labl.setText(m_lablValue);

        bb = m_labl.getBoundingBox();

        final double vtw = bb.getWidth();

        final double vth = bb.getHeight();

        final double rw = (ctw > vtw ? ctw : vtw) + TOOLTIP_PADDING_WIDTH;

        final double rh = (cth + vth) + TOOLTIP_PADDING_HEIGHT + 2;

        m_body.setWidth(rw).setHeight(rh).setCornerRadius(5);

        final double rx = m_body.getX();

        final double ry = m_body.getY();

        m_tail.setPoints(new Point2D(rx + rw / 2 - TRIANGLE_SIZE, ry + rh), new Point2D(rx + rw / 2, rh + TRIANGLE_SIZE), new Point2D(rx + rw / 2 + TRIANGLE_SIZE, ry + rh));

        m_mask.setPoints(new Point2D(rx + rw / 2 - TRIANGLE_SIZE - 3, ry + rh - 3), new Point2D(rx + rw / 2, rh + TRIANGLE_SIZE - 3), new Point2D(rx + rw / 2 + TRIANGLE_SIZE + 3, ry + rh - 3));

        final double vtx = rw / 2 - vtw / 2;

        final double ctx = rw / 2 - ctw / 2;

        final double vty = rh / 2 - vth / 2;

        final double cty = vty + cth + 2;

        m_text.setX(ctx).setY(cty);

        m_labl.setX(vtx).setY(vty - 1);

        setX(x - rw / 2);

        setY(y - rh);

        m_draw.add(this);

        moveToTop();

        m_draw.batch();

        return this;
    }

    public ToolTip hide()
    {
        if ((null == m_draw) || (true == m_show))
        {
            return this;
        }
        m_show = true;

        m_draw.remove(this);

        m_draw.batch();

        return this;
    }

    public ToolTip setValues(final String text, final String labl)
    {
        if (null == text)
        {
            m_textValue = "";
        }
        else
        {
            m_textValue = text;
        }
        if (null == labl)
        {
            m_lablValue = "";
        }
        else
        {
            m_lablValue = labl;
        }
        if (false == m_show)
        {
            show(m_oldx, m_oldy, true);
        }
        return this;
    }

    @Override
    public IStorageEngine<IPrimitive<?>> getDefaultStorageEngine()
    {
        return new PrimitiveFastArrayStorageEngine();
    }
}