/**
 * Copyright (C) 2013 - 2016 Johannes Taelman
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
 *
 * @author Mark Harris
 */
package axoloti;

import axoloti.object.AxoObject;

/**
 *
 * @author Mark Harris
 */
public interface IPlatform {

    void SetPatch(Patch p);

    void GoLive();

    void UploadDependentFiles();

    void Compile();

    void UploadToSDCard();

    void UploadToSDCard(String sdfilename);

    void Upload();
     
    //these 'raise' questions
    void WriteCode();

    AxoObject GenerateAxoObj();
}
