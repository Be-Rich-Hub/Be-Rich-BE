package org.example.berichbe.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

// ValueOfEnum 어노테이션이 String 타입의 필드에 있을 때 동작함
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, String> {

    private Class<? extends Enum<?>> enumClass; // 검증할 Enum 클래스를 저장

    @Override
    public void initialize(ValueOfEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass(); // 어노테이션에 담겨온 정보를 가져와 enumClass에 저장
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        // value는 클라이언트가 보낸 실제 문자열 값이 들어있음
        if (value == null) {
            return true; // @NotBlank 등으로 null 체크는 위임
        }
        // 입력된 문자열이 Enum 상수의 이름 중 하나와 일치하는지 확인
        return Stream.of(enumClass.getEnumConstants())
                .anyMatch(enumConstant -> enumConstant.name().equals(value.toUpperCase()));
    }
}

