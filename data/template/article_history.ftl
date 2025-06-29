<div class="container">
    <a href="/w/${articleName}" class="d-inline-block text-muted mb-4" style="text-decoration: none;">
        <i class="bi bi-arrow-left-short me-1"></i>
        Back
    </a>

    <h1 class="mb-5">History for ${articleName}</h1>

    <ul class="list-unstyled">
        <#if history?? && history?size gt 0>
            <#list history as item>
                <li class="list-group-item">${item}</li>
            </#list>
        <#else>
            <li class="list-group-item text-muted">No history to display.</li>
        </#if>
    </ul>
</div>