const API = "/api";

function showSection(section) {
  document.querySelectorAll(".section").forEach(s => {
    s.classList.remove("active");
  });

  document.getElementById(section).classList.add("active");

  if (section === "dashboard") {
    loadDashboard();
  }
}


// ================= HOUSEHOLD =================

document.getElementById("householdForm").addEventListener("submit", async function(e) {
  e.preventDefault();

  const data = {
    name: document.getElementById("householdName").value,
    address: document.getElementById("householdAddress").value,
    city: document.getElementById("householdCity").value,
    district: document.getElementById("householdDistrict").value,
    state: document.getElementById("householdState").value,
    email: document.getElementById("householdEmail").value,
    phNo: document.getElementById("householdPhone").value,
    members: Number(document.getElementById("householdMembers").value),
    carbonFootprint: Number(document.getElementById("householdCarbon").value)
  };

  const response = await fetch(`${API}/household/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();

  document.getElementById("householdResult").innerHTML =
      `<p>Household created successfully. ID: ${result.id}</p>`;

  this.reset();
  getHouseholds();
});


async function getHouseholds() {

  const response = await fetch(`${API}/household/getall`);
  const data = await response.json();

  document.getElementById("householdResult").innerHTML =
      createHouseholdTable(data);
}


function createHouseholdTable(data) {

  if (data.length === 0) {
    return "<p>No households found.</p>";
  }

  let html = `
        <table>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>City</th>
            <th>State</th>
            <th>Members</th>
            <th>Carbon</th>
        </tr>
    `;

  data.forEach(x => {
    html += `
        <tr>
            <td>${x.id}</td>
            <td>${x.name}</td>
            <td>${x.city}</td>
            <td>${x.state}</td>
            <td>${x.members}</td>
            <td>${x.carbonFootprint}</td>
        </tr>`;
  });

  html += "</table>";

  return html;
}


async function getHouseholdById() {

  const id = document.getElementById("searchHouseholdId").value;

  const response = await fetch(`${API}/household/getbyid/${id}`);

  if (!response.ok) {
    document.getElementById("householdResult").innerHTML =
        "<p>Household not found</p>";
    return;
  }

  const data = await response.json();

  document.getElementById("householdResult").innerHTML =
      createHouseholdTable([data]);
}


async function updateHousehold() {

  const data = {
    id: Number(document.getElementById("householdId").value),
    name: document.getElementById("householdName").value,
    address: document.getElementById("householdAddress").value,
    city: document.getElementById("householdCity").value,
    district: document.getElementById("householdDistrict").value,
    state: document.getElementById("householdState").value,
    email: document.getElementById("householdEmail").value,
    phNo: document.getElementById("householdPhone").value,
    members: Number(document.getElementById("householdMembers").value),
    carbonFootprint: Number(document.getElementById("householdCarbon").value)
  };

  const response = await fetch(`${API}/household/update`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();

  document.getElementById("householdResult").innerHTML =
      `<p>Household updated successfully.</p>`;
}


// ================= ACTIVITY =================

document.getElementById("activityForm").addEventListener("submit", async function(e) {

  e.preventDefault();

  const data = {
    activity: document.getElementById("activityName").value,
    category: document.getElementById("activityCategory").value,
    description: document.getElementById("activityDescription").value,
    date: document.getElementById("activityDate").value,
    householdName: document.getElementById("activityHousehold").value,
    carbonEmission: Number(document.getElementById("activityCarbon").value)
  };

  const response = await fetch(`${API}/activitylog/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();

  document.getElementById("activityResult").innerHTML =
      `<p>Activity created successfully. ID: ${result.id}</p>`;

  this.reset();
  getActivities();
});


async function getActivities() {

  const response = await fetch(`${API}/activitylog/getall`);
  const data = await response.json();

  let html = `
        <table>
        <tr>
            <th>ID</th>
            <th>Activity</th>
            <th>Category</th>
            <th>Date</th>
            <th>Carbon</th>
        </tr>
    `;

  data.forEach(x => {
    html += `
        <tr>
            <td>${x.id}</td>
            <td>${x.activity}</td>
            <td>${x.category}</td>
            <td>${x.date}</td>
            <td>${x.carbonEmission}</td>
        </tr>`;
  });

  html += "</table>";

  document.getElementById("activityResult").innerHTML = html;
}


async function getActivityById() {

  const id = document.getElementById("searchActivityId").value;

  const response = await fetch(`${API}/activitylog/getbyid/${id}`);

  if (!response.ok) {
    document.getElementById("activityResult").innerHTML =
        "<p>Activity not found</p>";
    return;
  }

  const data = await response.json();

  document.getElementById("activityResult").innerHTML =
      `<pre>${JSON.stringify(data, null, 2)}</pre>`;
}


async function updateActivity() {

  const data = {
    id: Number(document.getElementById("activityId").value),
    activity: document.getElementById("activityName").value,
    category: document.getElementById("activityCategory").value,
    description: document.getElementById("activityDescription").value,
    date: document.getElementById("activityDate").value,
    householdName: document.getElementById("activityHousehold").value,
    carbonEmission: Number(document.getElementById("activityCarbon").value)
  };

  await fetch(`${API}/activitylog/update`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  document.getElementById("activityResult").innerHTML =
      "<p>Activity updated successfully.</p>";
}


// ================= CHALLENGE =================

document.getElementById("challengeForm").addEventListener("submit", async function(e) {

  e.preventDefault();

  const data = {
    name: document.getElementById("challengeName").value,
    description: document.getElementById("challengeDescription").value,
    startDate: document.getElementById("challengeStart").value,
    endDate: document.getElementById("challengeEnd").value,
    target: Number(document.getElementById("challengeTarget").value),
    reward: Number(document.getElementById("challengeReward").value)
  };

  const response = await fetch(`${API}/challenge/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();

  document.getElementById("challengeResult").innerHTML =
      `<p>Challenge created successfully. ID: ${result.id}</p>`;

  this.reset();
  getChallenges();
});


async function getChallenges() {

  const response = await fetch(`${API}/challenge/getall`);
  const data = await response.json();

  let html = `
        <table>
        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Target</th>
            <th>Reward</th>
        </tr>
    `;

  data.forEach(x => {
    html += `
        <tr>
            <td>${x.id}</td>
            <td>${x.name}</td>
            <td>${x.description}</td>
            <td>${x.target}</td>
            <td>${x.reward}</td>
        </tr>`;
  });

  html += "</table>";

  document.getElementById("challengeResult").innerHTML = html;
}


async function getChallengeById() {

  const id = document.getElementById("searchChallengeId").value;

  const response = await fetch(`${API}/challenge/getbyid/${id}`);

  if (!response.ok) {
    document.getElementById("challengeResult").innerHTML =
        "<p>Challenge not found</p>";
    return;
  }

  const data = await response.json();

  document.getElementById("challengeResult").innerHTML =
      `<pre>${JSON.stringify(data, null, 2)}</pre>`;
}


async function updateChallenge() {

  const data = {
    id: Number(document.getElementById("challengeId").value),
    name: document.getElementById("challengeName").value,
    description: document.getElementById("challengeDescription").value,
    startDate: document.getElementById("challengeStart").value,
    endDate: document.getElementById("challengeEnd").value,
    target: Number(document.getElementById("challengeTarget").value),
    reward: Number(document.getElementById("challengeReward").value)
  };

  await fetch(`${API}/challenge/update`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  document.getElementById("challengeResult").innerHTML =
      "<p>Challenge updated successfully.</p>";
}


// ================= PARTICIPATION =================

document.getElementById("participationForm").addEventListener("submit", async function(e) {

  e.preventDefault();

  const data = {
    householdName: document.getElementById("participationHousehold").value,
    challengeName: document.getElementById("participationChallenge").value,
    joinDate: document.getElementById("participationDate").value,
    status: document.getElementById("participationStatus").value,
    carbonReduced: Number(document.getElementById("participationCarbon").value)
  };

  const response = await fetch(`${API}/participation/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  const result = await response.json();

  document.getElementById("participationResult").innerHTML =
      `<p>Participation created successfully. ID: ${result.id}</p>`;

  this.reset();
  getParticipations();
});


async function getParticipations() {

  const response = await fetch(`${API}/participation/getall`);
  const data = await response.json();

  let html = `
        <table>
        <tr>
            <th>ID</th>
            <th>Household</th>
            <th>Challenge</th>
            <th>Date</th>
            <th>Status</th>
            <th>Carbon Reduced</th>
        </tr>
    `;

  data.forEach(x => {
    html += `
        <tr>
            <td>${x.id}</td>
            <td>${x.householdName}</td>
            <td>${x.challengeName}</td>
            <td>${x.joinDate}</td>
            <td>${x.status}</td>
            <td>${x.carbonReduced}</td>
        </tr>`;
  });

  html += "</table>";

  document.getElementById("participationResult").innerHTML = html;
}


async function getParticipationById() {

  const id = document.getElementById("searchParticipationId").value;

  const response = await fetch(`${API}/participation/getbyid/${id}`);

  if (!response.ok) {
    document.getElementById("participationResult").innerHTML =
        "<p>Participation not found</p>";
    return;
  }

  const data = await response.json();

  document.getElementById("participationResult").innerHTML =
      `<pre>${JSON.stringify(data, null, 2)}</pre>`;
}


async function updateParticipation() {

  const data = {
    id: Number(document.getElementById("participationId").value),
    householdName: document.getElementById("participationHousehold").value,
    challengeName: document.getElementById("participationChallenge").value,
    joinDate: document.getElementById("participationDate").value,
    status: document.getElementById("participationStatus").value,
    carbonReduced: Number(document.getElementById("participationCarbon").value)
  };

  await fetch(`${API}/participation/update`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(data)
  });

  document.getElementById("participationResult").innerHTML =
      "<p>Participation updated successfully.</p>";
}


// ================= DASHBOARD =================

async function loadDashboard() {

  try {

    const households = await fetch(`${API}/household/getall`).then(r => r.json());
    const activities = await fetch(`${API}/activitylog/getall`).then(r => r.json());
    const challenges = await fetch(`${API}/challenge/getall`).then(r => r.json());
    const participations = await fetch(`${API}/participation/getall`).then(r => r.json());

    document.getElementById("householdCount").innerText = households.length;
    document.getElementById("activityCount").innerText = activities.length;
    document.getElementById("challengeCount").innerText = challenges.length;
    document.getElementById("participationCount").innerText = participations.length;

  } catch (error) {
    console.log(error);
  }
}

loadDashboard();