<div class="container">
    <h1 class="mb-5">Search results for ${terms}</h1>

    <#if results?? && results?size gt 0>
        <p>Found ${resultSize} result(s).</p>
    </#if>

    <ul class="list-unstyled">
            <#if results?? && results?size gt 0>
                <#list results as item>
                    <li class="list-group-item">
                        <a href="/w/${item.title}">${item.title}</a>
                    </li>
                </#list>
            <#else>
                <li class="list-group-item text-muted">No search results to display.</li>
            </#if>
        </ul>
</div>