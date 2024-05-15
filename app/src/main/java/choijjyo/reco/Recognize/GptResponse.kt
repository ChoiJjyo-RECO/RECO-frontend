package choijjyo.reco.Recognize

data class GptResponse(val choices: List<Choice>) {
    data class Choice(val text: String)
}