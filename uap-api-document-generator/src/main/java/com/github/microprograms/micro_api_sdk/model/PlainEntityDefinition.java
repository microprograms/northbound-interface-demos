package com.github.microprograms.micro_api_sdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PlainEntityDefinition implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 唯一标识符
	 */
	private String name;
	private String comment;
	private String description;
	private List<PlainFieldDefinition> fieldDefinitions;
	private PlainEntityMockConfig mock;
	private Map<String, Object> ext;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<PlainFieldDefinition> getFieldDefinitions() {
		return fieldDefinitions;
	}

	public void setFieldDefinitions(List<PlainFieldDefinition> fieldDefinitions) {
		this.fieldDefinitions = fieldDefinitions;
	}

	public PlainEntityMockConfig getMock() {
		return mock;
	}

	public void setMock(PlainEntityMockConfig mock) {
		this.mock = mock;
	}

	public Map<String, Object> getExt() {
		return ext;
	}

	public void setExt(Map<String, Object> ext) {
		this.ext = ext;
	}

	public static class PlainEntityMockConfig implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * 最小实例个数
		 */
		public final static int default_minInstanceCount = 1;
		/**
		 * 最大实例个数
		 */
		public final static int default_maxInstanceCount = 100;

		/**
		 * 最少实例个数
		 */
		private int minInstanceCount = default_minInstanceCount;
		/**
		 * 最大实例个数
		 */
		private int maxInstanceCount = default_maxInstanceCount;

		public int getMinInstanceCount() {
			return minInstanceCount;
		}

		public void setMinInstanceCount(int minInstanceCount) {
			this.minInstanceCount = minInstanceCount;
		}

		public int getMaxInstanceCount() {
			return maxInstanceCount;
		}

		public void setMaxInstanceCount(int maxInstanceCount) {
			this.maxInstanceCount = maxInstanceCount;
		}
	}

	public PlainEntityDefinition merge(PlainEntityDefinition overwrite) {
		if (null == overwrite) {
			return this;
		}

		if (StringUtils.isNotBlank(overwrite.name)) {
			name = overwrite.name;
		}
		if (StringUtils.isNotBlank(overwrite.comment)) {
			comment = overwrite.comment;
		}
		if (StringUtils.isNotBlank(overwrite.description)) {
			description = overwrite.description;
		}
		if (null != overwrite.fieldDefinitions) {
			if (null == fieldDefinitions) {
				fieldDefinitions = new ArrayList<>();
			}
			fieldDefinitions.addAll(overwrite.fieldDefinitions);
		}
		if (null != overwrite.mock) {
			mock = overwrite.mock;
		}
		if (null != overwrite.ext) {
			if (null == ext) {
				ext = new HashMap<>();
			}
			ext.putAll(overwrite.ext);
		}
		return this;
	}
}
