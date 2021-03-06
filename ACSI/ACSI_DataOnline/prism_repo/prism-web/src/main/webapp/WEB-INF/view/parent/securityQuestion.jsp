<c:if test="${not empty secretQuestionList}">
	<c:forEach var="userQuestion" items="${secretQuestionList}" varStatus="loopCount">	
		<c:if test="${loopCount.count le 3}">						
				<div class="field-block button-height noEnterSubmit">
					<label for="qsn${loopCount.count}" class="label"><b>${loopCount.count}. Choose a Question</b></label>	
						<select name="qsn${loopCount.count}"  id="qsn${loopCount.count}" class="select  qsn${loopCount.count} validate[required]">
							<c:forEach var="masterQuestion" items="${masterQuestionList}" varStatus="innerloopCount">
								<c:if test="${userQuestion.questionId == masterQuestion.questionId}">
									<option value=${masterQuestion.questionId} selected="selected">${masterQuestion.question}</option>								
								</c:if>
								<c:if test="${userQuestion.questionId != masterQuestion.questionId}">
									<option value=${masterQuestion.questionId} >${masterQuestion.question}</option>								
								</c:if>
							</c:forEach>
						</select>				
				</div>
				<div class="field-block button-height noEnterSubmit">
					<input type="hidden" name="ansId${loopCount.count}" id="ansId${loopCount.count}" value="${userQuestion.answerId}"  />
					<label for="ans${loopCount.count}" class="label"><b>Your Answer</b><span class="icon-star icon-size1 red"></span></label>
					<input type="text" name="ans${loopCount.count}" id="ans${loopCount.count}" value="${userQuestion.answer}" class="input validate[required]" maxlength="200">
				</div>		
		</c:if>					
	</c:forEach>
</c:if>
<c:if test="${empty secretQuestionList}">
	<div class="field-block button-height noEnterSubmit">
		<label for="qsn1" class="label"><b>1. Choose a Question</b></label>
		<select id="qsn1" name="qsn1" class="select  qsn1 validate[required]">
		<c:forEach var="secretInner" items="${masterQuestionList}" >
			<c:if test="${secretInner.sno == '1'}">
				<option value=${secretInner.questionId} selected="selected">${secretInner.question}</option>
			</c:if>
			<c:if test="${secretInner.sno != '1'}">
				<option value=${secretInner.questionId}>${secretInner.question}</option>
			</c:if>
		</c:forEach>
		</select>
	</div>
	
	<div class="field-block button-height noEnterSubmit">
		<input type="hidden" name="ansId1" id="ansId1" value="-1" />
		<label for="ans1" class="label"><b>Your Answer</b><span class="icon-star icon-size1 red"></span></label>
		<input type="text" name="ans1" id="ans1" value="" maxlength="199" class="input validate[required]" maxlength="200">
	</div>

	<div class="field-block button-height noEnterSubmit">
		<label for="qsn2" class="label"><b>2. Choose a Question</b></label>
		<select id="qsn2" name="qsn2" class="select qsn2 validate[required]">
			<c:forEach var="secretInner" items="${masterQuestionList}" >
				<c:if test="${secretInner.sno == '2'}">
					<option value=${secretInner.questionId} selected="selected">${secretInner.question}</option>
				</c:if>
				<c:if test="${secretInner.sno != '2'}">
					<option value=${secretInner.questionId}>${secretInner.question}</option>
				</c:if>
			</c:forEach>
		</select>
	</div>

	<div class="field-block button-height noEnterSubmit">
		<input type="hidden" name="ansId2" id="ansId2" value="-1" />
		<label for="ans2" class="label"><b>Your Answer</b><span class="icon-star icon-size1 red"></span></label>
		<input type="text" name="ans2" id="ans2" value="" maxlength="199" class="input validate[required]" maxlength="200">
	</div>

	<div class="field-block button-height noEnterSubmit">
		<label for="qsn3" class="label"><b>3. Choose a Question</b></label>
		<select id="qsn3" name="qsn3" class="select qsn3 validate[required]">
			<c:forEach var="secretInner" items="${masterQuestionList}" >
				<c:if test="${secretInner.sno == '3'}">
					<option value=${secretInner.questionId} selected="selected">${secretInner.question}</option>
				</c:if>
				<c:if test="${secretInner.sno != '3'}">
					<option value=${secretInner.questionId}>${secretInner.question}</option>
				</c:if>
			</c:forEach>
		</select>
	</div>

	<div class="field-block button-height noEnterSubmit">
		<input type="hidden" name="ansId3" id="ansId3" value="-1" />
		<label for="ans3" class="label"><b>Your Answer</b><span class="icon-star icon-size1 red"></span></label>
		<input type="text" name="ans3" id="ans3" value="" maxlength="199" class="input validate[required]" maxlength="200">
	</div>
</c:if>
