/* Copyright (c) 2009 Aur�lien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.FieldMethodizer;
import org.musicbrainz.search.index.TrackIndexField;

public class TrackHtmlWriter extends HtmlWriter {

	protected static final String TEMPLATE_RESOURCE_NAME = "track.html.vtl";

    public TrackHtmlWriter() throws Exception {
		super(TEMPLATE_RESOURCE_NAME);
	}

	@Override
	public void write(PrintWriter out, Results results, EnumMap<RequestParameter,String> extraInfoMap) throws IOException {
		
		VelocityContext context = new VelocityContext();
		context.put("offset", results.offset);
		context.put("totalHits", results.totalHits);
		context.put("results", results.results);
        context.put("updated",lastUpdated);

        context.put("Utils",Utils.class);
        context.put("Math", Math.class);
		// Make IndexField enum available in template context
		context.put("TrackIndexField", new FieldMethodizer( "org.musicbrainz.search.index.TrackIndexField" ));

        if(results.results.size()==1) {
            context.put("redirect",results.results.get(0).getDoc().get(TrackIndexField.TRACK_ID));
        }

        if(extraInfoMap.get(RequestParameter.TAGGER_PORT)!=null) {
            context.put("tport",extraInfoMap.get(RequestParameter.TAGGER_PORT));
            context.put("time",new Date().getTime());
        }
        else if(extraInfoMap.get(RequestParameter.OLD_STYLE_LINK)!=null) {
            context.put("oldstylelink","1");
        }
        else if(extraInfoMap.get(RequestParameter.RELATIONSHIPS)!=null) {
            context.put("relationships","1");
        }
        template.merge(context, out);
	}
	
}
