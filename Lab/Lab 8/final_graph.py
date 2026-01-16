import matplotlib.pyplot as plt
import pandas as pd

# Read CSV data
tahoe_data = pd.read_csv('TAHOE_metrics.csv')
reno_data = pd.read_csv('RENO_metrics.csv')

# Create single plot with dual y-axes
fig, ax1 = plt.subplots(figsize=(12, 8))

# Plot CWND on primary y-axis
ax1.plot(tahoe_data['Round'], tahoe_data['CWND'], 'r-o', label='TCP Tahoe CWND', markersize=4)
ax1.plot(reno_data['Round'], reno_data['CWND'], 'b-s', label='TCP Reno CWND', markersize=4)
ax1.set_xlabel('Transmission Round')
ax1.set_ylabel('Congestion Window (CWND)', color='black')
ax1.grid(True)

# Create secondary y-axis for SSThresh
ax2 = ax1.twinx()
ax2.plot(tahoe_data['Round'], tahoe_data['SSThresh'], 'r--', label='TCP Tahoe SSThresh', alpha=0.7)
ax2.plot(reno_data['Round'], reno_data['SSThresh'], 'b--', label='TCP Reno SSThresh', alpha=0.7)
ax2.set_ylabel('Slow Start Threshold (SSThresh)', color='black')

# Combine legends
lines1, labels1 = ax1.get_legend_handles_labels()
lines2, labels2 = ax2.get_legend_handles_labels()
ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left')

plt.title('TCP Tahoe vs Reno: CWND and SSThresh vs Transmission Round')
plt.tight_layout()
plt.savefig('tcp_comparison2.png', dpi=300, bbox_inches='tight')
print('Graph saved as tcp_comparison2.png')
plt.show()
