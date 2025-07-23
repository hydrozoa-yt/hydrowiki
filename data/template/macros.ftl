<#--
  Macro: numberClassSpan
  Description: Renders a <span> element with a class based on the number's sign.

  Parameters:
    number: The number to check (can be integer or decimal).
    content: The content to display inside the <span>.

  Classes applied:
    - "negative-number": If the number is less than 0.
    - "zero-number": If the number is equal to 0.
    - "positive-number": If the number is greater than 0.
-->
<#macro numberClassSpan number>
  <#if number < 0>
    <span class="text-danger">(${number})</span>
  <#elseif number == 0>
    <span class="text-secondary">(${number})</span>
  <#else>
    <span class="text-success">(${number})</span>
  </#if>
</#macro>