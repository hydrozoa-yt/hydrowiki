<div class="container">
    <h1 class="mb-5">Recent changes</h1>

    <ul class="list-unstyled">
        <#if history?? && history?size gt 0>
            <#list history as item>
                <li class="list-group-item">
                    ${item.timestamp}:
                    <a href="/w/${item.title}?diff=prev&id=${item.version}">${item.title}</a>
                </li>
            </#list>
        <#else>
            <li class="list-group-item text-muted">No recent changes to display.</li>
        </#if>
    </ul>
</div>