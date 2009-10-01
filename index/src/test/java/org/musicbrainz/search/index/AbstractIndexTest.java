package org.musicbrainz.search.index;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.BatchUpdateException;


public abstract class AbstractIndexTest extends TestCase {

    protected Connection createConnection() throws Exception {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:mem");

    }

    public void setup() throws Exception {
        try {
            Connection conn = createConnection();
            conn.setAutoCommit(true);

            //Drop tables, if they exist
            try {
                Statement stmt = conn.createStatement();
                stmt.addBatch("DROP TABLE country");

                stmt.addBatch("DROP TABLE artist");
                stmt.addBatch("DROP TABLE artist_alias");
                stmt.addBatch("DROP TABLE artist_name");
                stmt.addBatch("DROP TABLE artist_type");
                stmt.addBatch("DROP TABLE artist_credit");
                stmt.addBatch("DROP TABLE artist_credit_name");
                stmt.addBatch("DROP TABLE gender");

                stmt.addBatch("DROP TABLE label");
                stmt.addBatch("DROP TABLE label_alias");
                stmt.addBatch("DROP TABLE label_name");
                stmt.addBatch("DROP TABLE label_type");

                stmt.addBatch("DROP TABLE release_group");
                stmt.addBatch("DROP TABLE annotation");
                stmt.addBatch("DROP TABLE album");
                stmt.addBatch("DROP TABLE track");
                stmt.addBatch("DROP TABLE release");
                stmt.addBatch("DROP TABLE albummeta");
                stmt.addBatch("DROP TABLE language");
                stmt.addBatch("DROP TABLE script");
                stmt.addBatch("DROP TABLE albumjoin");
                stmt.executeBatch();
                stmt.close();
            }
            catch (BatchUpdateException bue) {
                //Ignore errors, because will just be that tables does not exist
                System.err.println(bue.getMessage());
            }

            //Create tables and data for Artist Index
            Statement stmt = conn.createStatement();

            stmt.addBatch("CREATE TABLE country" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  isocode character varying(2) NOT NULL," +
                    "  name character varying(100) NOT NULL" +
                    ")");

            setupArtistTables(stmt);
            setupLabelTables(stmt); 


            stmt.addBatch("CREATE TABLE album" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  artist integer NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  modpending integer DEFAULT 0," +
                    "  attributes array," +
                    "  page integer NOT NULL," +
                    "  language integer," +
                    "  script integer," +
                    "  modpending_lang integer," +
                    "  quality smallint ," +
                    "  modpending_qual integer DEFAULT 0," +
                    "  release_group integer NOT NULL" +
                    ")");

            stmt.addBatch("CREATE TABLE release_group" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  gid character(36)," +
                    "  name character varying(255)," +
                    "  page integer NOT NULL," +
                    "  artist integer NOT NULL," +
                    "  type integer," +
                    "  modpending integer DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE annotation" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  moderator integer NOT NULL," +
                    "  type smallint NOT NULL," +
                    "  rowid integer NOT NULL," +
                    "  text varchar(1000)," +
                    "  changelog character varying(255)," +
                    "  created timestamp," +
                    "  moderation integer NOT NULL DEFAULT 0," +
                    "  modpending integer NOT NULL DEFAULT 0" +
                    ")");


            stmt.addBatch("CREATE TABLE track" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  artist integer NOT NULL," +
                    "  name varchar(1000) NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  length integer DEFAULT 0," +
                    "  year integer DEFAULT 0," +
                    "  modpending integer DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE release" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  album integer NOT NULL," +
                    "  country integer NOT NULL," +
                    "  releasedate character(10) NOT NULL," +
                    "  modpending integer DEFAULT 0," +
                    "  label integer," +
                    "  catno character varying(255)," +
                    "  barcode character varying(255)," +
                    "  format smallint" +
                    ")");

            stmt.addBatch("CREATE TABLE albummeta" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  tracks integer DEFAULT 0," +
                    "  discids integer DEFAULT 0," +
                    "  puids integer DEFAULT 0," +
                    "  firstreleasedate character(10)," +
                    "  asin character(10)," +
                    "  coverarturl character varying(255)," +
                    "  lastupdate timestamp, " +
                    "  rating real," +
                    "  rating_count integer," +
                    "  dateadded timestamp" +
                    ")");

            stmt.addBatch("CREATE TABLE language" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  isocode_3t character(3) NOT NULL," +
                    "  isocode_3b character(3) NOT NULL," +
                    "  isocode_2 character(2)," +
                    "  name character varying(100) NOT NULL," +
                    "  frequency integer NOT NULL DEFAULT 0," +
                    "  CONSTRAINT language_pkey PRIMARY KEY (id)" +
                    ")");

            stmt.addBatch("CREATE TABLE script" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  isocode character(4) NOT NULL," +
                    "  isonumber character(3) NOT NULL," +
                    "  name character varying(100) NOT NULL," +
                    "  frequency integer NOT NULL DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE albumjoin" +
                    "(" +
                    "  id serial NOT NULL," +
                    "  album integer NOT NULL," +
                    "  track integer NOT NULL," +
                    "  sequence integer NOT NULL," +
                    "  modpending integer DEFAULT 0" +
                    ")");


            stmt.executeBatch();
            stmt.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    protected void setupArtistTables(Statement stmt) throws Exception {

        stmt.addBatch("CREATE TABLE artist (" +
                "  id serial NOT NULL," +
                "  gid character(36) NOT NULL," +
                "  name integer NOT NULL," +
                "  sortname integer NOT NULL," +
                "  begindate_year integer," +
                "  begindate_month integer," +
                "  begindate_day integer," +
                "  enddate_year integer," +
                "  enddate_month integer," +
                "  enddate_day integer," +
                "  type integer," +
                "  country integer," +
                "  gender integer," +
                "  comment character varying(255)," +
                "  editpending integer DEFAULT 0" +
                ")");

        stmt.addBatch("CREATE TABLE artist_credit (" +
                "  id serial NOT NULL," +
                "  artistcount integer NOT NULL," +
                "  refcount integer DEFAULT 0" +
                ")");

        stmt.addBatch("CREATE TABLE artist_credit_name (" +
                "  artist_credit integer NOT NULL," +
                "  position integer NOT NULL," +
                "  artist integer NOT NULL," +
                "  name integer NOT NULL," +
                "  joinphrase character varying(32)" +
                ")");

        stmt.addBatch("CREATE TABLE artist_alias (" +
                "  id serial NOT NULL," +
                "  artist integer NOT NULL," +
                "  name integer NOT NULL," +
                "  editpending integer NOT NULL DEFAULT 0" +
                ")");

        stmt.addBatch("CREATE TABLE artist_name (" +
                "  id serial NOT NULL," +
                "  name character varying(255) NOT NULL," +
                "  refcount integer DEFAULT 0" +
                ")");

        stmt.addBatch("CREATE TABLE artist_type (" +
                "  id serial NOT NULL," +
                "  name character varying(255) NOT NULL" +
                ")");

        stmt.addBatch("CREATE TABLE gender (" +
                "  id serial NOT NULL," +
                "  name character varying(255) NOT NULL" +
                ")");
    }

    protected void setupLabelTables(Statement stmt) throws Exception {
         stmt.addBatch("CREATE TABLE label (" +
                    "  id serial NOT NULL," +
                    "  gid character(36) NOT NULL," +
                    "  name integer NOT NULL," +
                    "  sortname integer NOT NULL," +
                    "  begindate_year integer," +
                    "  begindate_month integer," +
                    "  begindate_day integer," +
                    "  enddate_year integer," +
                    "  enddate_month integer," +
                    "  enddate_day integer," +
                    "  labelcode integer," +
                    "  type integer," +
                    "  country integer," +
                    "  comment character varying(255)," +
                    "  editpending integer DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE label_alias (" +
                    "  id serial NOT NULL," +
                    "  label integer NOT NULL," +
                    "  name integer NOT NULL," +
                    "  editpending integer NOT NULL DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE label_name (" +
                    "  id serial NOT NULL," +
                    "  name character varying(255) NOT NULL," +
                    "  refcount integer DEFAULT 0" +
                    ")");

            stmt.addBatch("CREATE TABLE label_type (" +
                    "  id serial NOT NULL," +
                    "  name character varying(255) NOT NULL" +
                    ")");
    }

}
