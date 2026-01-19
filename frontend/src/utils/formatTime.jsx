export default function formatTime(totalMinutes) {
  if (totalMinutes === null || totalMinutes === undefined || totalMinutes < 0) {
    return "";
  }
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;

  let runtimeString = "";
  if (hours > 0) {
    runtimeString += `${hours}h `;
  }
  if (minutes > 0 || hours === 0) {
    runtimeString += `${minutes}min`;
  }

  return runtimeString.trim();
}
