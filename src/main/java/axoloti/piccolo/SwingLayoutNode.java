/*
 * Copyright (c) 2008-2011, Piccolo2D project, http://piccolo2d.org
 * Copyright (c) 1998-2008, University of Maryland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 * and the following disclaimer in the documentation and/or other materials provided with the
 * distribution.
 *
 * None of the name of the University of Maryland, the name of the Piccolo2D project, or the names of its
 * contributors may be used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

 /*
This started as the Piccolo project's implementation, but has been heavily modified for Axoloti.
 */
package axoloti.piccolo;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.border.Border;
import org.piccolo2d.PNode;

public class SwingLayoutNode extends PNode {

    private final ProxyComponent proxyComponent;

    public SwingLayoutNode() {
        this.proxyComponent = new ProxyComponent(this);
    }

    public void addChild(final int index, final SwingLayoutNode child) {
        /*
         * NOTE: This must be the only super.addChild call that we make in our
         * entire implementation, because all PNode.addChild methods are
         * implemented in terms of this one. Calling other variants of
         * super.addChild will incorrectly invoke our overrides, resulting in
         * StackOverflowException.
         */
        super.addChild(index, child);
        addProxyComponent(child);
    }

    public void addChild(final SwingLayoutNode child) {
        // NOTE: since PNode.addChild(PNode) is implemented in terms of
        // PNode.addChild(int index), we must do the same.
        int index = getChildrenCount();
        // workaround a flaw in PNode.addChild(PNode), they should have handled
        // this in PNode.addChild(int index).
        if (child.getParent() == this) {
            index--;
        }
        addChild(index, child);
    }

    public void addChildren(final Collection nodes) {
        final Iterator i = nodes.iterator();
        while (i.hasNext()) {
            final SwingLayoutNode each = (SwingLayoutNode) i.next();
            addChild(each);
        }
    }

    public PNode removeChild(final int index) {
        /*
         * NOTE: This must be the only super.removeChild call that we make in
         * our entire implementation, because all PNode.removeChild methods are
         * implemented in terms of this one. Calling other variants of
         * super.removeChild will incorrectly invoke our overrides, resulting in
         * StackOverflowException.
         */
        final SwingLayoutNode node = (SwingLayoutNode) super.removeChild(index);
        removeProxyComponent(node);
        return node;
    }

    public void removeAllChildren() {
        final Iterator i = getChildrenIterator();
        while (i.hasNext()) {
            removeChild((SwingLayoutNode) i.next());
        }
    }

    private void addProxyComponent(final SwingLayoutNode node) {
        proxyComponent.add(node.getProxyComponent());
    }

    private void removeProxyComponent(final SwingLayoutNode node) {
        if (node != null) {
            proxyComponent.remove(node.getProxyComponent());
        }
    }

    public ProxyComponent getProxyComponent() {
        return proxyComponent;
    }

    public static class ProxyComponent extends JComponent {

        private final SwingLayoutNode node;

        public ProxyComponent(final SwingLayoutNode node) {
            this.node = node;
        }

        public void setBounds(final int x, final int y, final int w, final int h) {
            if (x != getX() || y != getY() || w != getWidth() || h != getHeight()) {
                super.setBounds(x, y, w, h);

                node.setOffset(x, y);
                double width = w;
                // handle swing's annoying parent relative negative widths
                if (width < 0) {
                    width = ((SwingLayoutNode) node.getParent()).getProxyComponent().getWidth() + w;
                }
                node.setBounds(0, 0, width, h);
                node.getProxyComponent().doLayout();
            }
        }
    }

    public void setAlignmentX(float alignmentX) {
        proxyComponent.setAlignmentX(alignmentX);
    }

    public void setAlignmentY(float alignmentY) {
        proxyComponent.setAlignmentY(alignmentY);
    }

    public float getAlignmentX() {
        return proxyComponent.getAlignmentX();
    }

    public float getAlignmentY() {
        return proxyComponent.getAlignmentY();
    }

    public Dimension getPreferredSize() {
        return proxyComponent.getPreferredSize();
    }

    public void setPreferredSize(Dimension d) {
        proxyComponent.setPreferredSize(d);
    }

    public void setSize(Dimension d) {
        proxyComponent.setSize(d);
    }

    public Dimension getSize() {
        return proxyComponent.getSize();
    }

    public void setMinimumSize(Dimension d) {
        proxyComponent.setMinimumSize(d);
    }

    public Dimension getMinimumSize() {
        return proxyComponent.getMinimumSize();
    }

    public void setMaximumSize(Dimension d) {
        proxyComponent.setMaximumSize(d);
    }

    public Dimension getMaximumSize() {
        return proxyComponent.getMaximumSize();
    }

    public void setBorder(Border border) {
        proxyComponent.setBorder(border);
    }

    public void addToSwingProxy(Component c) {
        proxyComponent.add(c);
    }

    public void validate() {

    }

    public void setLayout(LayoutManager manager) {
        proxyComponent.setLayout(manager);
    }

    public int roundUp(double val) {
        return (int) Math.ceil(val);
    }
}
