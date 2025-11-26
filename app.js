async function loadApod() {
    try {
        const response = await fetch("http://localhost:8081/api/apod/today");
        const data = await response.json();

        document.getElementById("title").innerText = data.title;
        document.getElementById("image").src = data.url;
        document.getElementById("explanation").innerText = data.explanation;

    } catch (error) {
        console.error("Error loading APOD:", error);
        document.getElementById("title").innerText = "Error loading APOD data";
    }
}

loadApod();
