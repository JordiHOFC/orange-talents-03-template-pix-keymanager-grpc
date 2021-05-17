package br.com.zup.edu.chavepix.validator

import javax.validation.Constraint
import javax.validation.ReportAsSingleViolation
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@ReportAsSingleViolation
@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.EXPRESSION,AnnotationTarget.PROPERTY,AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@Pattern(regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",flags = [Pattern.Flag.CASE_INSENSITIVE])
@Constraint(validatedBy = [])
annotation class ValidUUID (
    val message: String = "Chave no formato Invalido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

