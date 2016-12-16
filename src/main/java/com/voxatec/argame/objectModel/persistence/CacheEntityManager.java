package com.voxatec.argame.objectModel.persistence;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Vector;

import org.springframework.web.util.HtmlUtils;

import com.voxatec.argame.objectModel.beans.City;
import com.voxatec.argame.objectModel.beans.File;
import com.voxatec.argame.objectModel.beans.CacheGroup;
import com.voxatec.argame.objectModel.beans.Cache;



public class CacheEntityManager extends EntityManager {

	// ---- Get all caches with cities -----------------------------------------------------------------------
	public Vector<City> getCityCaches() throws SQLException {
		Vector<City> cityList = new Vector<City>();
		Vector<CacheGroup> cacheGroupList = null;
		Vector<Cache> cacheList = null;

		try {
			this.initConnection();

			String stmt = "select * from v_city_cache";
			ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			City city = null;
			CacheGroup cacheGroup = null;
			Cache cache = null;			

			while (resultSet.next()) {
				// City
				int cityId = resultSet.getInt("city_id");
				if (city == null || cityId != city.getId()) {
					city = new City();
					city.setId(resultSet.getInt("city_id"));
					city.setName(HtmlUtils.htmlEscape(resultSet.getString("city_name")));
					city.setZip(HtmlUtils.htmlEscape(resultSet.getString("city_zip")));
					city.setCountry(HtmlUtils.htmlEscape(resultSet.getString("city_country")));
					cacheGroupList = new Vector<CacheGroup>();
					city.setCacheGroupList(cacheGroupList);
					cityList.add(city);
				}

				// CacheGroup
				int cacheGroupId = resultSet.getInt("cgrp_id");
				if (!resultSet.wasNull() && (cacheGroup == null || cacheGroupId != cacheGroup.getId())) {
					cacheGroup = new CacheGroup();
					cacheGroup.setId(cacheGroupId);
					cacheGroup.setName(HtmlUtils.htmlEscape(resultSet.getString("cgrp_name")));
					cacheGroup.setText(HtmlUtils.htmlEscape(resultSet.getString("cgrp_text")));
					cacheGroup.setCityId(cityId);
					cacheGroup.setTargetImageDatFileName(HtmlUtils.htmlEscape(resultSet.getString("target_img_dat_file_name")));
					cacheGroup.setTargetImageXmlFileName(HtmlUtils.htmlEscape(resultSet.getString("target_img_xml_file_name")));
					cacheList = new Vector<Cache>();
					cacheGroup.setCacheList(cacheList);
					cacheGroupList.add(cacheGroup);
				}
				
				// Cache
				int cacheId = resultSet.getInt("cache_id");
				if (!resultSet.wasNull()) {
					cache = new Cache();
					cache.setId(cacheId);
					cache.setName(HtmlUtils.htmlEscape(resultSet.getString("cache_name")));
					cache.setText(HtmlUtils.htmlEscape(resultSet.getString("cache_text")));
					cache.setStreet(HtmlUtils.htmlEscape(resultSet.getString("street")));
					cache.setTargetImageFileName(HtmlUtils.htmlEscape(resultSet.getString("target_img_file_name")));
					cache.setGpsLatitude(resultSet.getBigDecimal("gps_lat"));
					cache.setGpsLongitude(resultSet.getBigDecimal("gps_long"));
					cacheList.add(cache);
				}
			}

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}

		return cityList;
	}
	
	
	// ---- Create a cache-group -----------------------------------------------------------------------
	public CacheGroup createCacheGroup(CacheGroup cacheGroup) throws SQLException {
		
		if (cacheGroup == null)
			return null;
		
		try {
			this.initConnection();

			// Insert cacheGroup into DB
			String template = "insert into cache_group (city_id,name) values (%d,\"%s\",\"%s\")";
			Integer cityId = cacheGroup.getCityId();
			String name = HtmlUtils.htmlUnescape(cacheGroup.getName());
			String text = this.queryfiableString(HtmlUtils.htmlUnescape(cacheGroup.getText()));
			String stmt = String.format(template, cityId, name, text);
			this.connection.executeUpdateStatement(stmt);
			
			// Retrieve auto inserted ID value
			Integer lastInsertedId = this.getLastAutoInsertedId();
			cacheGroup.setId(lastInsertedId);
			
		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
		
		return cacheGroup;
	}
	
	
	// ---- Update a cache-group -----------------------------------------------------------------------
	public void updateCacheGroup(CacheGroup cacheGroup) throws SQLException {
		
		if (cacheGroup == null)
			return;
		
		try {
			this.initConnection();

			String template = "update cache_group set name=\"%s\", text=\"%s\" where id=%d";
			String name = HtmlUtils.htmlUnescape(cacheGroup.getName());
			String text = this.queryfiableString(HtmlUtils.htmlUnescape(cacheGroup.getText()));
			Integer id  = cacheGroup.getId();
			String stmt = String.format(template, name, text, id);
			
			this.connection.executeUpdateStatement(stmt);

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
	}
	
	
	// ---- Get a cache-group by ID -----------------------------------------------------------------------
	public CacheGroup getCacheGroupById(Integer cacheGroupId) throws SQLException {
		
		CacheGroup theCacheGroup = null;
		
		try {
			this.initConnection();

			String template = "select * from cache_group where id=%d";
			String stmt = String.format(template, cacheGroupId);
			ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			while (resultSet.next()) {
				// Adventure
				theCacheGroup = new CacheGroup();
				theCacheGroup.setId(cacheGroupId);
				theCacheGroup.setName(HtmlUtils.htmlEscape(resultSet.getString("name")));
			}

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}

		return theCacheGroup;
	}
	
	
	// ---- Delete a cache-group -----------------------------------------------------------------------
	public void deleteCacheGroup(Integer cacheGroupId) throws SQLException {
		
		CacheGroup theCacheGroup = null;
		
		if (cacheGroupId == EntityManager.UNDEF_ID)
			return;   // nothing to do
		
		try {
			theCacheGroup = this.getCacheGroupById(cacheGroupId);
			
			this.initConnection();
			
			if (theCacheGroup != null) {
				// Delete cacheGropu
				String template = "delete from cache_group where id=%d";
				String stmt = String.format(template, cacheGroupId);
				this.connection.executeUpdateStatement(stmt);
			}
		
		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
	}
	
	
	// ---- Get XML file of a cache-group ---------------------------------------------------------------------
	public File getXmlFile(Integer cacheGroupId) throws SQLException {
		File xmlFile = new File();
		
        try {
			this.initConnection();

	        String template = "select target_img_xml_file_name, target_img_xml_file from cache_group where id=%d";
	        String stmt = String.format(template, cacheGroupId);
	        ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			if (resultSet.next()) {
				Blob blob = resultSet.getBlob("target_img_xml_file");
				if (blob != null) {
					String strContent = new String(blob.getBytes(1l, (int) blob.length()));
					xmlFile.setContent(HtmlUtils.htmlEscape(strContent));
					xmlFile.setMimeType("application/octet-stream");
				}
				xmlFile.setName(resultSet.getString("target_img_xml_file_name"));
        	}
			
        } catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
			
		} finally {
			this.connection.close();
		}
		
		return xmlFile;
	}
	

	// ---- Update XML file of a cache-group ----------------------------------------------------------------------
	public void updateXmlFile(File xmlFile, Integer cacheGroupId) throws SQLException {
		
		try {
			this.initConnection();
			
			PreparedStatement stmt = this.connection.newPreparedStatement("update cache_group set target_img_xml_file=?, target_img_xml_file_name=? where id=?");
			
			// Unescape file content
			String fileContent = HtmlUtils.htmlUnescape(xmlFile.getContent());
			
			// Bind statement parameters & execute
			Blob blob = this.connection.newBlob();
			blob.setBytes(1, fileContent.getBytes());
			stmt.setBlob(1, blob);
	        stmt.setString(2, xmlFile.getName());
	        stmt.setInt(3, cacheGroupId);
	        stmt.executeUpdate();
			
		} catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
		} finally {
			this.connection.close();
		}
	}
	
	
	// ---- Get DAT file of object group ---------------------------------------------------------------------
	public File getDatFile(Integer cacheGroupId) throws SQLException {
		File datFile = new File();
		
        try {
			this.initConnection();

	        String template = "select target_img_dat_file_name, target_img_dat_file from cache_group where id=%d";
	        String stmt = String.format(template, cacheGroupId);
	        ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			if (resultSet.next()) {
				Blob blob = resultSet.getBlob("target_img_dat_file");
				if (blob != null) {
					String strContent = new String(blob.getBytes(1l, (int) blob.length()));
					datFile.setContent(HtmlUtils.htmlEscape(strContent));
					datFile.setMimeType("application/octet-stream");
				}
				datFile.setName(resultSet.getString("target_img_dat_file_name"));
        	}
			
        } catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
			
		} finally {
			this.connection.close();
		}
		
		return datFile;
	}

	
	// ---- Update DAT file of a cache-group ----------------------------------------------------------------------
	public void updateDatFile(File datFile, Integer cacheGroupId) throws SQLException {
		
		try {
			this.initConnection();
			
			PreparedStatement stmt = this.connection.newPreparedStatement("update cache_group set target_img_dat_file=?, target_img_dat_file_name=? where id=?");
			
			// Decode image from Base64
			byte[] fileContent = null;
			if (datFile != null && datFile.getContent() != null) {
				byte[] bytes = datFile.getContent().getBytes();
				fileContent = Base64.getDecoder().decode(bytes);
			}
			
			// Bind statement parameters & execute
			Blob blob = this.connection.newBlob();
			blob.setBytes(1, fileContent);
			stmt.setBlob(1, blob);
	        stmt.setString(2, datFile.getName());
	        stmt.setInt(3, cacheGroupId);
	        stmt.executeUpdate();
			
		} catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
		} finally {
			this.connection.close();
		}
	}
	
	
	// ---- Create a cache -----------------------------------------------------------------------
	public Cache createCache(Cache cache) throws SQLException {
		
		if (cache == null)
			return null;
		
		try {
			this.initConnection();

			// Insert cacheGroup into DB
			String template = "insert into cache (cache_grp_id,name,text,street,gps_lat,gps_long) values (%d,\"%s\",\"%s\",\"%s\",%f,%f)";
			Integer cacheGroupId = cache.getCacheGroupId();
			String name = HtmlUtils.htmlUnescape(cache.getName());
			String text = this.queryfiableString(HtmlUtils.htmlUnescape(cache.getText()));
			String street = HtmlUtils.htmlUnescape(cache.getStreet());
			BigDecimal gpsLat = cache.getGpsLatitude();
			BigDecimal gpsLong = cache.getGpsLongitude();
			String stmt = String.format(template, cacheGroupId, name, text, street, gpsLat, gpsLong);
			this.connection.executeUpdateStatement(stmt);
			
			// Retrieve auto inserted ID value
			Integer lastInsertedId = this.getLastAutoInsertedId();
			cache.setId(lastInsertedId);
			
		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
		
		return cache;
	}
	
	
	// ---- Update a cache -----------------------------------------------------------------------
	public void updateCache(Cache cache) throws SQLException {
		
		if (cache == null)
			return;
		
		try {
			this.initConnection();

			String template = "update cache set name=\"%s\", text=\"%s\", street=\"%s\", gps_lat=%f, gps_long=%f where id=%d";
			String name = HtmlUtils.htmlUnescape(cache.getName());
			String text = this.queryfiableString(HtmlUtils.htmlUnescape(cache.getText()));
			String street = HtmlUtils.htmlUnescape(cache.getStreet());
			BigDecimal gpsLat = cache.getGpsLatitude();
			BigDecimal gpsLong = cache.getGpsLongitude();
			Integer id  = cache.getId();
			String stmt = String.format(template, name, text, street, gpsLat, gpsLong, id);
			
			
			this.connection.executeUpdateStatement(stmt);

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
	}
	
	
	// ---- Get a cache by ID -----------------------------------------------------------------------
	public Cache getCacheById(Integer cacheId) throws SQLException {
		
		Cache theCache = null;
		
		try {
			this.initConnection();

			String template = "select * from cache where id=%d";
			String stmt = String.format(template, cacheId);
			ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			while (resultSet.next()) {
				// Cache
				theCache = new Cache();
				theCache.setId(cacheId);
				theCache.setName(HtmlUtils.htmlEscape(resultSet.getString("name")));
				theCache.setText(HtmlUtils.htmlEscape(resultSet.getString("text")));
				theCache.setStreet(HtmlUtils.htmlEscape(resultSet.getString("street")));
				theCache.setGpsLatitude(resultSet.getBigDecimal("gps_lat"));
				theCache.setGpsLongitude(resultSet.getBigDecimal("gps_long"));
			}

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}

		return theCache;
	}
	
	
	// ---- Delete a cache -----------------------------------------------------------------------
	protected void deleteCacheReferences(Integer cacheId) throws SQLException {
		
		if (cacheId == EntityManager.UNDEF_ID)
			return;   // nothing to do

		try {
			// Delete cache references from scene_cache mapping table
			String template = "delete from scene_cache where cache_id=%d";
			String stmt = String.format(template, cacheId);
			this.connection.executeUpdateStatement(stmt);

		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;
		}
	}
	
	
	public void deleteCache(Integer cacheId) throws SQLException {
		
		Cache theCache = null;
		
		if (cacheId == EntityManager.UNDEF_ID)
			return;   // nothing to do
		
		try {
			theCache = this.getCacheById(cacheId);
			
			this.initConnection();
			
			if (theCache != null) {
				// Delete references to cache first
				this.deleteCacheReferences(cacheId);
				
				// Delete cache
				String template = "delete from cache where id=%d";
				String stmt = String.format(template, cacheId);
				this.connection.executeUpdateStatement(stmt);
			}
		
		} catch (SQLException exception) {
			System.out.print(exception.toString());
			throw exception;

		} finally {
			this.connection.close();
		}
	}
	
	// ---- Get target image of cache --------------------------------------------------------------------
	public byte[] getTargetImageFile(Integer cacheId) throws SQLException {
		byte[] imgData = "".getBytes();
		
		try {
			this.initConnection();
			
	        String template = "select target_img_file from cache where id=%d";
	        String stmt = String.format(template, cacheId);
	        ResultSet resultSet = this.connection.executeSelectStatement(stmt);

			if (resultSet.next()) {
				Blob blob = resultSet.getBlob("target_img_file");
				if (blob != null) {
					imgData = blob.getBytes(1, (int) blob.length());
				}
			}
			
		} catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
			
		} finally {
			this.connection.close();
		}
		
		return imgData;
	}
	
	
	// ---- Update target image of cache -------------------------------------------------------------------
	public void updateTargetImageFile(File imageFile, Integer cacheId) throws SQLException, UnsupportedEncodingException {
		
		try {
			System.out.println(String.format("content size: %d", imageFile.getContent().length()));

			this.initConnection();
			PreparedStatement stmt = this.connection.newPreparedStatement("update cache set target_img_file=?, target_img_file_name=? where id=?");
			
			// Decode image from Base64
			byte[] image = null;
			if (imageFile != null && imageFile.getContent() != null) {
				byte[] bytes = imageFile.getContent().getBytes();
				image = Base64.getDecoder().decode(bytes);
			}
			
			// Bind statement parameters & execute
			String imageFileName = HtmlUtils.htmlUnescape(imageFile.getName());
			Blob blob = this.connection.newBlob();
			blob.setBytes(1, image);
			stmt.setBlob(1, blob);
			stmt.setString(2, imageFileName);
	        stmt.setInt(3, cacheId);
	        stmt.executeUpdate();
			
		} catch (Exception exception) {
			System.out.print(exception.toString());
			throw exception;        	
		} finally {
			this.connection.close();
		}
	
	}

}