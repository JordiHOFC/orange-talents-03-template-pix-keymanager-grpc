package br.com.zup.edu.chavepix

public fun validaValorParachave(chave: String): Boolean {
    when {
        chave.matches("\\+[1-9][0-9]\\d{1,14}".toRegex()) -> {
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