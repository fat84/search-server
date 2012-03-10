/* Copyright (c) 2009 Lukas Lalinsky
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

package org.musicbrainz.search.servlet.mmd1;

import com.jthink.brainz.mmd.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistCreditHelper;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class ReleaseMmd1XmlWriter extends Mmd1XmlWriter {


    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        ReleaseList releaseList = of.createReleaseList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Release release = of.createRelease();
            release.setId(doc.get(ReleaseIndexField.RELEASE_ID));

            String type = doc.get(ReleaseIndexField.TYPE);
            String status = doc.get(ReleaseIndexField.STATUS);
            if (isNotUnknown(type) || isNotUnknown(status)) {
                if (isNotUnknown(type)) {
                    release.getType().add(StringUtils.capitalize(type));
                }

                if (isNotUnknown(status)) {
                    release.getType().add(status);
                }
            }

            release.getOtherAttributes().put(getScore(), String.valueOf((int) (result.score * 100)));

            String name = doc.get(ReleaseIndexField.RELEASE);
            if (name != null) {
                release.setTitle(name);

            }

            String asin = doc.get(ReleaseIndexField.AMAZON_ID);
            if (isNotNoValue(asin)) {
                release.setAsin(asin);

            }

            TextRepresentation tr = of.createTextRepresentation();
            String script = doc.get(ReleaseIndexField.SCRIPT);
            if (isNotUnknown(script)) {
                tr.setScript(script);
            }
            String lang = doc.get(ReleaseIndexField.LANGUAGE);
            if (isNotUnknown(lang)) {
                tr.setLanguage(lang.toUpperCase(Locale.US));
            }

            if (script != null || lang != null) {
                release.setTextRepresentation(tr);
            }

            String country = doc.get(ReleaseIndexField.COUNTRY);
            String date = doc.get(ReleaseIndexField.DATE);
            String barcode = doc.get(ReleaseIndexField.BARCODE);
            String format = doc.get(ReleaseIndexField.FORMAT);

            String[] labelNames = doc.getValues(ReleaseIndexField.LABEL);
            //Now releases can only have multiple labe;/catno combinations but MMDv1
            //expects country,date,barcode and format to also be part of each release event.
            if (labelNames.length > 0) {
                ReleaseEventList eventList = of.createReleaseEventList();
                String[] catnos = doc.getValues(ReleaseIndexField.CATALOG_NO);
                String[] labelIds = doc.getValues(ReleaseIndexField.LABEL_ID);

                for (int i = 0; i < labelNames.length; i++) {
                    Event event = of.createEvent();

                    if (isNotNoValue(labelNames[i]) || isNotNoValue(labelIds[i])) {
                        Label label = of.createLabel();
                        event.setLabel(label);
                        
                        if (isNotNoValue(labelNames[i])) {
                            label.setName(labelNames[i]);
                        }
                        
                        if (isNotNoValue(labelIds[i])) {
                            label.setId(labelIds[i]);
                        }
                    }

                    if (isNotNoValue(catnos[i])) {
                        event.setCatalogNumber(catnos[i]);
                    }

                    if (isNotUnknown(country)) {
                        event.setCountry(StringUtils.upperCase(country));
                    }

                    if (event != null) {
                        event.setDate(date);
                    }

                    if (isNotNoValue(barcode)) {
                        event.setBarcode(barcode);
                    }

                    if (isNotNoValue(format)) {
                        event.setFormat(format);
                    }
                    eventList.getEvent().add(event);
                }
                release.setReleaseEventList(eventList);
            }
            else {
                ReleaseEventList eventList = of.createReleaseEventList();
                Event event = of.createEvent();

                if (country != null) {
                    event.setCountry(StringUtils.upperCase(country));
                }

                if (event != null) {
                    event.setDate(date);
                }

                if (barcode != null) {
                    event.setBarcode(barcode);
                }

                if (format != null) {
                    event.setFormat(format);
                }
                eventList.getEvent().add(event);
                release.setReleaseEventList(eventList);                    
            }

            //Just add the first Artist (if there are more than one, this means that once releases get added with multiple
            //name credits using this old interface isn't going to give very good results
            if(doc.get(ReleaseIndexField.ARTIST_CREDIT)!=null) {
                ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseIndexField.ARTIST_CREDIT));
                if (ac.getNameCredit().size()>0) {
                    Artist artist = of.createArtist();
                    artist.setName(ac.getNameCredit().get(0).getArtist().getName());
                    artist.setId(ac.getNameCredit().get(0).getArtist().getId());
                    artist.setSortName(ac.getNameCredit().get(0).getArtist().getSortName());
                    release.setArtist(artist);
                }
            }

            String[] numDiscsIdsOnMedium = doc.getValues(ReleaseIndexField.NUM_DISCIDS_MEDIUM);
            if(numDiscsIdsOnMedium.length>0)
            {
                int numDiscs = 0;
                for(int i=0;i<numDiscsIdsOnMedium.length;i++) {
                    numDiscs+=NumericUtils.prefixCodedToInt(numDiscsIdsOnMedium[i]);
                }

                DiscList discList = of.createDiscList();
                discList.setCount(BigInteger.valueOf(numDiscs));
                release.setDiscList(discList);
            }

            String[] numTracksOnMedium = doc.getValues(ReleaseIndexField.NUM_TRACKS_MEDIUM);
            if(numTracksOnMedium.length>0)
            {
                int numTracks = 0;
                for(int i=0;i<numTracksOnMedium.length;i++) {
                    numTracks+=NumericUtils.prefixCodedToInt(numTracksOnMedium[i]);
                }

                TrackList trackList = of.createTrackList();
                trackList.setCount(BigInteger.valueOf(numTracks));
                release.setTrackList(trackList);
            }
            releaseList.getRelease().add(release);
        }
        releaseList.setCount(BigInteger.valueOf(results.totalHits));
        releaseList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setReleaseList(releaseList);
        return metadata;
    }
}
