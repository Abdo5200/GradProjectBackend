const backendUrl = "http://localhost:8080";
const token = localStorage.getItem("token");

if (!token) {
    alert("Please log in first!");
    window.location.href = "index.html";
}

document.getElementById("uploadBtn").addEventListener("click", async () => {
    const fileInput = document.getElementById("fileInput");
    const messageEl = document.getElementById("message");

    if (fileInput.files.length === 0) {
        messageEl.textContent = "Please select a file.";
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append("file", file);
    formData.append("folder", "images/");

    try {
        const response = await fetch(`${backendUrl}/api/files/upload`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`
            },
            body: formData
        });

        const data = await response.json();

        if (response.ok) {
            messageEl.textContent = "✅ File uploaded successfully!";
            const imgUrl = data.url;

            // ✅ Save image URL to localStorage
            localStorage.setItem("uploadedImageUrl", imgUrl);

            // ✅ Redirect to view page
            setTimeout(() => {
                window.location.href = "view-image.html";
            }, 1000);
        } else {
            messageEl.textContent = data.error || "❌ Upload failed.";
        }

    } catch (err) {
        messageEl.textContent = "⚠️ Error connecting to server.";
    }
});
