/**
 * Copyright (C) 2013, 2014 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti.displays;

import axoloti.displayviews.DisplayInstanceViewBool32;

/**
 *
 * @author Johannes Taelman
 */
public class DisplayInstanceBool32<DisplayBool32> extends DisplayInstanceInt32 {

    public DisplayInstanceBool32() {
        super();
    }

    @Override
    public DisplayInstanceViewBool32 ViewFactory() {
        return new DisplayInstanceViewBool32(this);
    }
}
