<div class="container">
    <h1 class="mb-5">New article</h1>
    <form>
        <!-- Article Title Field -->
        <div class="mb-4">
            <label for="articleTitle" class="form-label">Article Title</label>
            <input
                type="text"
                id="articleTitle"
                name="articleTitle"
                placeholder="Enter article title"
                class="form-control"
                required
            >
        </div>

        <!-- Article Content Field -->
        <div class="mb-4">
            <label for="articleContent" class="form-label">Article Content</label>
            <textarea
                id="articleContent"
                name="articleContent"
                rows="8"
                placeholder="Write your article content here..."
                class="form-control"
                required
            ></textarea>
        </div>

        <!-- Submit Button -->
        <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-primary d-inline-flex align-items-center" type="button">
                Add article
                <i class="bi bi-plus-circle-fill ms-2" style="font-size: 2rem;"></i>
            </button>
        </div>
    </form>
</div>