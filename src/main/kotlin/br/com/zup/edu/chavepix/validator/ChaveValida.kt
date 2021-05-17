package br.com.zup.edu.chavepix

import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@Constraint(validatedBy = [ChaveValidaValidator::class])
annotation class ChaveValida (
    val message: String = "Chave no formato Invalido",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Any>> = [],
)

@Singleton
class ChaveValidaValidator(private val repository: ChaveRepository): ConstraintValidator<ChaveValida, String> {
    override fun isValid(
        chave: String?,
        annotationMetadata: AnnotationValue<ChaveValida>,
        context: ConstraintValidatorContext
    ): Boolean {
        when {
            chave!!.matches("\\+[1-9][0-9]\\d{1,14}".toRegex()) -> {
                return true
            }
            chave.matches("[0-9]{11}".toRegex()) -> {
                if (validaCpf(chave)){
                    return true
                }
                return false
            }
            chave.matches("^[A-Za-z0-9+_.-]+@(.+)\$".toRegex()) -> {
                return true
            }
            else -> return false
        }

    }

}