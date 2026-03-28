package io.cmartinezs.keygo.run.config;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SnakeCaseModelConverter}.
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class SnakeCaseModelConverterTest {

    private SnakeCaseModelConverter converter;

    @Mock
    private ModelConverterContext context;

    @Mock
    private ModelConverter nextConverter;

    @BeforeEach
    void setUp() {
        converter = new SnakeCaseModelConverter();
    }

    // ─── toSnakeCase static helper ─────────────────────────────────────────────

    @ParameterizedTest(name = "''{0}'' → ''{1}''")
    @CsvSource({
        "clientId,         client_id",
        "redirectUris,     redirect_uris",
        "createdAt,        created_at",
        "updatedAt,        updated_at",
        "profilePictureUrl,profile_picture_url",
        "firstName,        first_name",
        "lastName,         last_name",
        "tenantId,         tenant_id",
        "phoneNumber,      phone_number",
        "totalElements,    total_elements",
        "totalPages,       total_pages",
        "ownerEmail,       owner_email",
        "clientSecret,     client_secret",
    })
    void toSnakeCase_shouldConvertCamelToSnake(String input, String expected) {
        assertThat(SnakeCaseModelConverter.toSnakeCase(input.trim()))
            .isEqualTo(expected.trim());
    }

    @ParameterizedTest(name = "''{0}'' already snake_case → unchanged")
    @CsvSource({
        "access_token",
        "id_token",
        "refresh_token",
        "token_type",
        "expires_in",
        "id",
        "name",
        "status",
        "email",
        "scope",
    })
    void toSnakeCase_shouldLeaveSnakeCaseUnchanged(String input) {
        assertThat(SnakeCaseModelConverter.toSnakeCase(input)).isEqualTo(input);
    }

    @Test
    void toSnakeCase_shouldReturnNullForNull() {
        assertThat(SnakeCaseModelConverter.toSnakeCase(null)).isNull();
    }

    @Test
    void toSnakeCase_shouldReturnEmptyForEmpty() {
        assertThat(SnakeCaseModelConverter.toSnakeCase("")).isEmpty();
    }

    // ─── resolve() ────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resolve_shouldRenamePropertiesFromCamelToSnake() {
        // Given
        Schema<Object> schema = new Schema<>();
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("clientId", new Schema<>());
        props.put("redirectUris", new Schema<>());
        props.put("createdAt", new Schema<>());
        schema.setProperties(props);

        Iterator<ModelConverter> chain = List.of(nextConverter).iterator();
        when(nextConverter.resolve(any(AnnotatedType.class), any(ModelConverterContext.class), any()))
            .thenReturn(schema);

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, chain);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProperties()).containsKeys("client_id", "redirect_uris", "created_at");
        assertThat(result.getProperties()).doesNotContainKeys("clientId", "redirectUris", "createdAt");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resolve_shouldLeaveAlreadySnakeCasePropertiesUnchanged() {
        // Given
        Schema<Object> schema = new Schema<>();
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("access_token", new Schema<>());
        props.put("id_token", new Schema<>());
        props.put("expires_in", new Schema<>());
        schema.setProperties(props);

        Iterator<ModelConverter> chain = List.of(nextConverter).iterator();
        when(nextConverter.resolve(any(AnnotatedType.class), any(ModelConverterContext.class), any()))
            .thenReturn(schema);

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, chain);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProperties()).containsKeys("access_token", "id_token", "expires_in");
    }

    @Test
    @SuppressWarnings("rawtypes")
    void resolve_shouldReturnNullWhenChainIsEmpty() {
        // Given
        Iterator<ModelConverter> emptyChain = Collections.emptyIterator();

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, emptyChain);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings("rawtypes")
    void resolve_shouldReturnNullWhenDelegateReturnsNull() {
        // Given
        Iterator<ModelConverter> chain = List.of(nextConverter).iterator();
        when(nextConverter.resolve(any(AnnotatedType.class), any(ModelConverterContext.class), any()))
            .thenReturn(null);

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, chain);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resolve_shouldNotMutateSchemaWithNoProperties() {
        // Given
        Schema<Object> schema = new Schema<>();
        // no properties set

        Iterator<ModelConverter> chain = List.of(nextConverter).iterator();
        when(nextConverter.resolve(any(AnnotatedType.class), any(ModelConverterContext.class), any()))
            .thenReturn(schema);

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, chain);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProperties()).isNull();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void resolve_shouldHandleMixedCamelAndSnakeProperties() {
        // Given — schema with a mix (e.g. from @JsonProperty + plain fields)
        Schema<Object> schema = new Schema<>();
        Map<String, Schema> props = new LinkedHashMap<>();
        props.put("access_token", new Schema<>());   // already snake_case (@JsonProperty)
        props.put("expiresIn",    new Schema<>());   // camelCase
        props.put("tokenType",    new Schema<>());   // camelCase
        schema.setProperties(props);

        Iterator<ModelConverter> chain = List.of(nextConverter).iterator();
        when(nextConverter.resolve(any(AnnotatedType.class), any(ModelConverterContext.class), any()))
            .thenReturn(schema);

        // When
        Schema result = converter.resolve(new AnnotatedType(), context, chain);

        // Then
        assertThat(result.getProperties())
            .containsKeys("access_token", "expires_in", "token_type")
            .doesNotContainKeys("expiresIn", "tokenType");
    }
}




