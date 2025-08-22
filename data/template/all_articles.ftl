<div class="container">
    <h1 class="mb-5">All articles</h1>

    <ul class="list-unstyled">
        <#if articles?? && articles?size gt 0>
            <#list articles as item>
                <li class="list-group-item">
                    <a href="/w/${item.title}">${item.titleHumanReadable}</a>
                </li>
            </#list>
        <#else>
            <li class="list-group-item text-muted">No articles to display.</li>
        </#if>
    </ul>
</div>