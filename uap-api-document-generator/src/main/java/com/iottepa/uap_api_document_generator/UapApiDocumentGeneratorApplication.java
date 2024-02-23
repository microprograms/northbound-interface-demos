package com.iottepa.uap_api_document_generator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.alibaba.fastjson.JSONObject;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.plugin.table.LoopRowTableRenderPolicy;
import com.github.microprograms.micro_api_sdk.model.ApiDefinition;
import com.github.microprograms.micro_api_sdk.model.ModuleDefinition;
import com.github.microprograms.micro_api_sdk.model.PlainEntityDefinition;
import com.github.microprograms.micro_api_sdk.model.PlainFieldDefinition;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

@SpringBootApplication
public class UapApiDocumentGeneratorApplication implements CommandLineRunner {

	private static Logger log = LoggerFactory.getLogger(UapApiDocumentGeneratorApplication.class);

	public static void main(String[] args) throws Exception {
		SpringApplication.run(UapApiDocumentGeneratorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		for (String arg : args) {
			log.info("命令行参数: {}", arg);
		}

		String json = StringUtils.join(readClasspathResourceAsLines("uap-apis.json"), "");
		Map<String, ?> document = (Map<String, ?>) Configuration.defaultConfiguration().jsonProvider().parse(json);

		ModuleDefinition moduleDefinition = new ModuleDefinition();
		List<ApiDefinition> apiDefinitions = new ArrayList<>();
		moduleDefinition.setApiDefinitions(apiDefinitions);

		List<Map<String, ?>> apis = JsonPath.read(document, "$.apis[*]");
		apis.forEach(x -> apiDefinitions.add(buildApiDefinition(x, document)));
		IntStream.range(0, apiDefinitions.size()).forEach(i -> apiDefinitions.get(i).getExt().put("number", i + 1));

		List<PlainEntityDefinition> modelDefinitions = new ArrayList<>();
		moduleDefinition.setModelDefinitions(modelDefinitions);
		List<Map<String, ?>> entities = JsonPath.read(document, "$.entities[*]");
		entities.forEach(x -> {
			PlainEntityDefinition entity = new PlainEntityDefinition();
			entity.setName((String) x.get("type"));
			entity.setComment((String) x.get("desc"));
			PlainEntityDefinition merge = buildEntityFromFields(x.get("definition"), document);
			modelDefinitions.add(entity.merge(merge));
		});
		IntStream.range(0, modelDefinitions.size()).forEach(i -> {
			PlainEntityDefinition modelDefinition = modelDefinitions.get(i);
			if (null == modelDefinition.getExt()) {
				modelDefinition.setExt(new HashMap<>());
			}
			modelDefinition.getExt().put("number", i + 1);
		});

		// System.out.println(buildEntitySchema("SyncAssetDetailEntity",
		// moduleDefinition));

		LoopRowTableRenderPolicy loopRowTableRenderPolicy = new LoopRowTableRenderPolicy();
		Configure config = Configure.builder().bind("apiDefinitions",
				loopRowTableRenderPolicy)
				.bind("requestHeaderDefinition.fieldDefinitions", loopRowTableRenderPolicy)
				.bind("requestDefinition.fieldDefinitions", loopRowTableRenderPolicy)
				.bind("responseDefinition.fieldDefinitions", loopRowTableRenderPolicy)
				.bind("fieldDefinitions", loopRowTableRenderPolicy).build();
		XWPFTemplate template = XWPFTemplate.compile("C:/Users/test/Downloads/北向接口文档v2.docx", config).render(
				new HashMap<String, Object>() {
					{
						put("apiDefinitions", moduleDefinition.getApiDefinitions());
						put("modelDefinitions", moduleDefinition.getModelDefinitions());
					}
				});
		template.writeAndClose(new FileOutputStream("C:/Users/test/Downloads/output.docx"));
	}

	public static ApiDefinition buildApiDefinition(Map<String, ?> rawApiDefinition, Map<String, ?> document) {
		ApiDefinition apiDefinition = new ApiDefinition();
		apiDefinition.setName((String) rawApiDefinition.get("title"));
		apiDefinition.setComment((String) rawApiDefinition.get("desc"));
		apiDefinition.setExt(new HashMap<>());
		apiDefinition.getExt().put("uri", (String) rawApiDefinition.get("uri"));
		apiDefinition.getExt().put("method", (String) rawApiDefinition.get("method"));
		apiDefinition.setRequestHeaderDefinition(buildEntityFromFields(rawApiDefinition.get("headers"), document));
		apiDefinition.setRequestDefinition(buildEntityFromFields(rawApiDefinition.get("request"), document));
		apiDefinition.setResponseDefinition(buildEntityFromFields(rawApiDefinition.get("response"), document));
		return apiDefinition;
	}

	public static PlainEntityDefinition buildEntityFromFields(Object rawFieldDefinitions, Map<String, ?> document) {
		if (null == rawFieldDefinitions) {
			return null;
		}

		if (rawFieldDefinitions instanceof Map) {
			Map<String, ?> map = (Map<String, ?>) rawFieldDefinitions;
			return (PlainEntityDefinition) buildRef((String) map.get("_refFlat"), true, document);
		} else if (rawFieldDefinitions instanceof List) {
			List<?> list = (List<?>) rawFieldDefinitions;
			PlainEntityDefinition entity = new PlainEntityDefinition();
			List<PlainFieldDefinition> fields = new ArrayList<>();
			entity.setFieldDefinitions(fields);
			list.forEach(x -> {
				Map<String, ?> map = (Map<String, ?>) x;
				PlainFieldDefinition ref = (PlainFieldDefinition) buildRef((String) map.get("_ref"), false, document);
				fields.add(mergeRef(map, ref));
			});
			return entity;
		} else {
			throw new RuntimeException("Unsupported type");
		}
	}

	public static PlainFieldDefinition mergeRef(Map<String, ?> map, PlainFieldDefinition ref) {
		PlainFieldDefinition field = new PlainFieldDefinition();
		field.setName((String) map.get("name"));
		field.setJavaType((String) map.get("type"));
		field.setExample(map.get("example"));
		field.setComment((String) map.get("desc"));
		field.setRequired((Boolean) map.get("required"));
		return null == ref ? field : ref.merge(field);
	}

	public static PlainFieldDefinition replaceGenericsType(PlainFieldDefinition plainFieldDefinition,
			String genericsType) {
		if (null == plainFieldDefinition || StringUtils.isBlank(plainFieldDefinition.getJavaType())
				|| StringUtils.isBlank(genericsType)) {
			return plainFieldDefinition;
		}
		String replacedJavaType = plainFieldDefinition.getJavaType().replaceAll("<GenericsType>", genericsType);
		plainFieldDefinition.setJavaType(replacedJavaType);
		return plainFieldDefinition;
	}

	public static Object buildRef(String _ref, boolean isFlat, Map<String, ?> document) {
		if (StringUtils.isBlank(_ref)) {
			return null;
		}

		String genericsType = null;
		Pattern pattern = Pattern.compile("([^<]+)<([^>]+)>");
		Matcher matcher = pattern.matcher(_ref);
		if (matcher.matches()) {
			_ref = matcher.group(1);
			genericsType = matcher.group(2);
			log.debug("buildRef with genericsType, _ref={}, genericsType={}", _ref, genericsType);
		}

		JSONArray jsonPathQueryResult = JsonPath.read(document,
				String.format("$.exports[?(@.type=='%s')].definition", _ref));
		if (jsonPathQueryResult.isEmpty()) {
			return null;
		}

		Object result = jsonPathQueryResult.get(0);
		if (result instanceof Map && isFlat) {
			throw new RuntimeException("_refFlat command cannot reference Map");
		}

		if (result instanceof List && !isFlat) {
			throw new RuntimeException("_ref command cannot reference List");
		}

		if (result instanceof Map) {
			Map<String, ?> map = (Map<String, ?>) result;
			PlainFieldDefinition ref = (PlainFieldDefinition) buildRef((String) map.get("_ref"), false, document);
			PlainFieldDefinition mergeRef = mergeRef(map, ref);
			return replaceGenericsType(mergeRef, genericsType);
		} else if (result instanceof List) {
			List<?> list = (List<?>) result;
			PlainEntityDefinition entity = new PlainEntityDefinition();
			List<PlainFieldDefinition> fields = new ArrayList<>();
			entity.setFieldDefinitions(fields);
			final String _genericsType = genericsType;
			list.forEach(x -> {
				Map<String, ?> map = (Map<String, ?>) x;
				PlainFieldDefinition ref = (PlainFieldDefinition) buildRef((String) map.get("_ref"), false, document);
				PlainFieldDefinition mergeRef = mergeRef(map, ref);
				fields.add(replaceGenericsType(mergeRef, _genericsType));
			});
			return entity;
		} else {
			return null;
		}
	}

	public static List<String> readClasspathResourceAsLines(String classpathResourceName) throws IOException {
		try (InputStream in = UapApiDocumentGeneratorApplication.class.getClassLoader()
				.getResourceAsStream(classpathResourceName)) {
			return IOUtils.readLines(in, "utf8");
		}
	}

	public static JSONObject buildEntitySchema(String type, ModuleDefinition moduleDefinition) throws IOException {
		if (CollectionUtils.isEmpty(moduleDefinition.getModelDefinitions())) {
			return null;
		}
		Optional<PlainEntityDefinition> plainEntityDefinitionOptional = moduleDefinition.getModelDefinitions().stream()
				.filter(x -> type.equals(x.getName())).findFirst();
		if (plainEntityDefinitionOptional.isEmpty()) {
			return null;
		}
		JSONObject json = new JSONObject();
		for (PlainFieldDefinition x : plainEntityDefinitionOptional.get().getFieldDefinitions()) {
			boolean isArray = x.getJavaType().endsWith("[]");
			Pattern pattern = Pattern.compile("<([^>]+)>.*");
			Matcher matcher = pattern.matcher(x.getJavaType());
			if (matcher.matches()) {
				String nestedType = matcher.group(1);
				JSONObject nestedEntitySchema = buildEntitySchema(nestedType, moduleDefinition);
				json.put(x.getName(), isArray ? Arrays.asList(nestedEntitySchema) : nestedEntitySchema);
			} else {
				String fieldComment = String.format("<%s>", x.getComment());
				json.put(x.getName(), isArray ? Arrays.asList(fieldComment) : fieldComment);
			}
		}
		return json;
	}

}
