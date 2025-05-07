package se.gu.conll_u

import java.io.File

class CoNLLUParseException(event: CoNLLUEvent?, file: File, line: Long, cause: Throwable) :
    Exception("Processing corpus failed on ${file.path}, line $line:\n$event", cause)
