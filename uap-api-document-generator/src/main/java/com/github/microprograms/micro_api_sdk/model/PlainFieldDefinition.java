package com.github.microprograms.micro_api_sdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class PlainFieldDefinition implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String comment;
	private String description;
	private Object example;
	private List<Object> examples;
	private String javaType;
	private Boolean required;
	private Integer primaryKey;
	private Object defaultValue;
	private String mock;
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

	public Object getExample() {
		return example;
	}

	public void setExample(Object example) {
		this.example = example;
	}

	public List<Object> getExamples() {
		return examples;
	}

	public void setExamples(List<Object> examples) {
		this.examples = examples;
	}

	public String getJavaType() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Integer getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Integer primaryKey) {
		this.primaryKey = primaryKey;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getMock() {
		return mock;
	}

	public void setMock(String mock) {
		this.mock = mock;
	}

	public Map<String, Object> getExt() {
		return ext;
	}

	public void setExt(Map<String, Object> ext) {
		this.ext = ext;
	}

	public PlainFieldDefinition merge(PlainFieldDefinition overwrite) {
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
		if (null != overwrite.example) {
			example = overwrite.example;
		}
		if (null != overwrite.examples) {
			if (null == examples) {
				examples = new ArrayList<>();
			}
			examples.addAll(overwrite.examples);
		}
		if (StringUtils.isNotBlank(overwrite.javaType)) {
			javaType = overwrite.javaType;
		}
		if (null != overwrite.required) {
			required = overwrite.required;
		}
		if (null != overwrite.primaryKey) {
			primaryKey = overwrite.primaryKey;
		}
		if (null != overwrite.defaultValue) {
			defaultValue = overwrite.defaultValue;
		}
		if (StringUtils.isNotBlank(overwrite.mock)) {
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
