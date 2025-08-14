<div class="container">
    <div class="d-flex justify-content-end mb-4">
        <#if loggedIn>
            <a href="?action=delete" class="btn btn-danger rounded-element me-2" onclick="return confirm('Are you sure you want to delete this? This action cannot be undone.');">
                <i class="bi bi-trash3 me-2"></i>
                Delete
            </a>
        </#if>
    </div>
<div>
<div class="container">
    <a href="/w/${articleName}" class="d-inline-block text-muted mb-4" style="text-decoration: none;">
        <i class="bi bi-arrow-left-short me-1"></i>
        Back
    </a>
    <#if infoMessage?? && infoMessage?has_content>
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${infoMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </#if>

    <h1 class="mb-5">Edit ${articleNameHumanReadable}</h1>

    <form method="post">
        <!-- Article Content Field -->
        <div class="mb-4">
            <label for="articleContent" class="form-label">Content</label>
            <textarea
                id="articleContent"
                name="articleContent"
                rows="8"
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
</div>