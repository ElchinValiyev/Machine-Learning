import matplotlib.pyplot as plt

plt.style.use('ggplot')

for i in range(6):
    samples = []
    centers = []

    for line in open("centers" + str(i) + ".txt", 'r'):
        centers.append([float(x) for x in line.strip().split(",")])

    centers_x = [point[0] for point in centers]
    centers_y = [point[1] for point in centers]

    plt.scatter(centers_x, centers_y, c='black',s=100)

    for line in open("samples" + str(i) + ".txt", 'r'):
        samples.append([float(x) for x in line.strip().split(",")])

    x = [point[0] for point in samples if point[2] < 0.5]
    y = [point[1] for point in samples if point[2] < 0.5]

    a = [point[0] for point in samples if point[2] > 0.5 and point[2] < 1.5]
    b = [point[1] for point in samples if point[2] > 0.5 and point[2] < 1.5]

    d = [point[0] for point in samples if point[2] > 1.5]
    e = [point[1] for point in samples if point[2] > 1.5]

    plt.scatter(x, y, c='b')
    plt.scatter(a, b, c='r')
    plt.scatter(d, e, c='g')
    plt.show()
