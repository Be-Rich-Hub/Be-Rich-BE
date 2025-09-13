package org.example.berichbe.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD}) // 이 어노테이션은 필드에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 어노테이션의 정보가 런타임에도 유지
@Constraint(validatedBy = ValueOfEnumValidator.class) // ValueOfEnum 어노테이션이 붙은 필드는 ValueOfEnumValidator 클래스에 있는 로직을 사용해서 유효성 검증
public @interface ValueOfEnum {
    String message() default "유효하지 않은 Enum 값입니다."; // 유효성 검증 실패했을 때 기본 에러 메시지
    Class<?>[] groups() default {}; // 검증 규칙을 그룹화하는 항목
    Class<? extends Payload>[] payload() default {}; // 검증 실패 시 심각도 등 부가 정보를 담는 항목
    Class<? extends Enum<?>> enumClass(); // 어떤 Enum 클래스를 기준으로 검증할지를 직접 지정하게 해줌
}
