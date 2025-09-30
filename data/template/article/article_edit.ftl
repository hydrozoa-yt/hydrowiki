<#import "../macros.ftl" as macros>
<div class="container">

    <#if infoMessage2??>
        <@macros.infoMessage message=infoMessage2 />
    </#if>

    <div class="d-flex justify-content-between align-items-center mb-3">
        <a href="/w/${articleName}" class="d-inline-block text-muted mb-4" style="text-decoration: none;">
            <i class="bi bi-arrow-left-short me-1"></i>
            Back
        </a>
        <div>
            <#if loggedIn>
                <a href="?action=delete" class="btn btn-danger rounded-element" onclick="return confirm('Are you sure you want to delete this? This action cannot be undone.');">
                    <i class="bi bi-trash3 me-2"></i>
                    Delete
                </a>
            </#if>
        </div>
    </div>

    <h1 class="mb-5">Edit ${articleNameHumanReadable}</h1>

    <form method="post">
        <!-- Article Content Field -->
        <div class="mb-4">
            <label for="articleContent" class="form-label">Content</label>
            <textarea
                id="articleContent"
                name="articleContent"
                rows="20"
                class="form-control"
                required
            >${articleContentCode}</textarea>
        </div>

        <!-- Submit Button -->
        <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-primary d-inline-flex align-items-center" type="button">
                <i class="bi bi-floppy2-fill me-2"></i>
                Save
            </button>
        </div>
    </form>

    <hr class="mb-4 mt-4">

    <h2 class="mb-5">Aliases</h1>

    <ul class="list-unstyled mb-4">
        <#if aliases??>
            <#list aliases as item>
                <li class="list-group-item">
                    ${item}
                </li>
            </#list>
        <#else>
            <li class="list-group-item">
                No registered aliases.
            </li>
        </#if>
    </ul>

    <h3>Add alias</h1>
    <form method="post">
        <div class="mb-4">
            <label for="addAlias" class="form-label">Alias</label>
            <input
                id="addAlias"
                name="addAlias"
                class="form-control"
                required
            ></textarea>
        </div>

        <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-primary d-inline-flex align-items-center" type="button">
                <i class="bi bi-plus-lg me-2"></i>
                Add
            </button>
        </div>
    </form>

</div>